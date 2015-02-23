/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package parser;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.content.CompIfaceContent;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.hfsm.ImplHfsm;
import fun.hfsm.State;
import fun.hfsm.StateComposite;
import fun.hfsm.StateContent;
import fun.hfsm.StateSimple;
import fun.hfsm.Transition;
import fun.other.CompImpl;
import fun.other.FunList;
import fun.other.Template;
import fun.statement.Block;
import fun.variable.ConstPrivate;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;

public class ImplHfsmParser extends ImplBaseParser {
  public ImplHfsmParser(Scanner scanner) {
    super(scanner);
  }

  public static CompImpl parse(Scanner scanner, String name) {
    ImplHfsmParser parser = new ImplHfsmParser(scanner);
    return parser.parseImplementationHfsm(name);
  }

  // EBNF implementationComposition: "hfsm" compDeclBlock stateBody
  private CompImpl parseImplementationHfsm(String name) {
    ElementInfo info = expect(TokenType.HFSM).getInfo();

    FunList<CompIfaceContent> iface = new FunList<CompIfaceContent>();

    while (peek().getType() != TokenType.STATE) {
      Token id = expect(TokenType.IDENTIFIER);
      expect(TokenType.COLON);
      CompIfaceContent obj = (CompIfaceContent) parseIfaceDeclaration(id.getData());

      iface.add(obj);
    }

    State top = parseState("!top");

    expect(TokenType.END);

    ImplHfsm implHfsm = new ImplHfsm(info, name, top);
    implHfsm.getInterface().addAll(iface);
    return implHfsm;
  }

  // as in implCompositionParser
  private FuncHeader parseIfaceDeclaration(String name) {
    switch (peek().getType()) {
      case RESPONSE:
      case SLOT:
      case SIGNAL:
      case QUERY:
        return parseFuncDef(peek().getType(), name, true);
      default: {
        // return type().parseTypedecl();
        RError.err(ErrorType.Error, peek().getInfo(), "Expected interface function");
        return null;
      }
    }
  }

  // EBNF stateBody: { entryCode | exitCode | varDeclBlock | funcDecl | transitionDecl | state }
  private <T extends State> void parseStateBody(T state) {
    Block entryBody = new Block(state.getInfo());
    Block exitBody = new Block(state.getInfo());
    initEntryExit(state, entryBody, exitBody);

    while (!consumeIfEqual(TokenType.END)) {
      switch (peek().getType()) {
        case ENTRY:
          entryBody.getStatements().add(parseEntryCode());
          break;
        case EXIT:
          exitBody.getStatements().add(parseExitCode());
          break;
        default:
          Token id = expect(TokenType.IDENTIFIER);
          switch (peek().getType()) {
            case EQUAL:
              expect(TokenType.EQUAL);
              List<TemplateParameter> genpam;
              if (peek().getType() == TokenType.OPENCURLY) {
                genpam = parseGenericParam();
              } else {
                genpam = new ArrayList<TemplateParameter>();
              }
              FuncHeader obj = parseStateDeclaration(id.getData());
              state.getItemList().add(new Template(id.getInfo(), id.getData(), genpam, obj));
              break;
            case COLON:
              expect(TokenType.COLON);
              Fun inst = parseStateInstantiation(id.getData());
              state.getItemList().add((StateContent) inst);
              break;
            default:
              state.getItemList().add(parseTransition(id));
              break;
          }
      }
    }
  }

  private Fun parseStateInstantiation(String name) {
    switch (peek().getType()) {
      case STATE:
        return parseState(name);
      case CONST:
        ConstPrivate con = parseConstDef(ConstPrivate.class, name);
        expect(TokenType.SEMI);
        return con;
      case RESPONSE:
        return parseFuncDef(peek().getType(), name, false);
      default:
        StateVariable var = parseVarDef2(StateVariable.class, name);
        expect(TokenType.SEMI);
        return var;
    }
  }

  private FuncHeader parseStateDeclaration(String name) {
    switch (peek().getType()) {
      case FUNCTION:
      case PROCEDURE:
        return parseFuncDef(peek().getType(), name, false);
      default: {
        // return type().parseTypedecl();
        RError.err(ErrorType.Error, peek().getInfo(), "Expected function");
        return null;
      }
    }
  }

  private void initEntryExit(State state, Block entryBody, Block exitBody) {
    state.setEntryFunc(entryBody);
    state.setExitFunc(exitBody);
  }

  // EBNF stateDecl: "state" ( ";" | ( [ "(" id ")" ] stateBody ) )
  private State parseState(String name) {
    ElementInfo info = expect(TokenType.STATE).getInfo();

    State state;
    if (consumeIfEqual(TokenType.SEMI)) {
      state = new StateSimple(info, name);
      initEntryExit(state, new Block(state.getInfo()), new Block(state.getInfo()));
    } else {
      if (consumeIfEqual(TokenType.OPENPAREN)) {
        String initial = expect(TokenType.IDENTIFIER).getData();
        expect(TokenType.CLOSEPAREN);
        state = new StateComposite(info, name, new Reference(info, new DummyLinkTarget(info, initial)));
      } else {
        state = new StateSimple(info, name);
      }

      parseStateBody(state);
    }
    return state;
  }

  // EBNF transition: nameRef "to" nameRef "by" transitionEvent [ "if" expr ] ( ";" | "do" block "end" )
  private Transition parseTransition(Token id) {
    Reference src = parseNameRef(id);
    ElementInfo info = expect(TokenType.TO).getInfo();
    Reference dst = parseNameRef();

    Transition ret = new Transition(info);
    ret.setSrc(src);
    ret.setDst(dst);

    expect(TokenType.BY);
    parseTransitionEvent(ret);
    {
      Expression guard;
      if (consumeIfEqual(TokenType.IF)) {
        guard = expr().parse();
      } else {
        guard = new BoolValue(info, true);
      }
      ret.setGuard(guard);
    }
    {
      Block body;
      if (consumeIfEqual(TokenType.SEMI)) {
        body = new Block(info);
      } else {
        expect(TokenType.DO);
        body = stmt().parseBlock();
        expect(TokenType.END);
      }
      ret.setBody(body);
    }
    return ret;
  }

  // EBNF nameRef: Designator
  private Reference parseNameRef() {
    return parseNameRef(expect(TokenType.IDENTIFIER));
  }

  private Reference parseNameRef(Token tokh) {
    Reference ret = new Reference(tokh.getInfo(), tokh.getData());

    while (consumeIfEqual(TokenType.PERIOD)) {
      Token tok = expect(TokenType.IDENTIFIER);
      ret.getOffset().add(new RefName(tok.getInfo(), tok.getData()));
    }

    return ret;
  }

  // EBNF transitionEvent: id vardeflist
  private void parseTransitionEvent(Transition ret) {
    Token tok = expect(TokenType.IDENTIFIER);
    Reference name = new Reference(tok.getInfo(), tok.getData());
    ret.setEvent(name);
    ret.getParam().addAll(parseVardefList());
  }

}
