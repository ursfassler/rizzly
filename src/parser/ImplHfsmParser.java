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

import parser.scanner.Scanner;
import parser.scanner.Token;
import parser.scanner.TokenType;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.component.hfsm.StateComposite;
import evl.data.component.hfsm.StateContent;
import evl.data.component.hfsm.StateSimple;
import evl.data.component.hfsm.Transition;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.reference.DummyLinkTarget;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.function.header.FuncProcedure;
import evl.data.function.ret.FuncReturnNone;
import evl.data.statement.Block;
import evl.data.variable.ConstPrivate;
import evl.data.variable.FuncVariable;
import evl.data.variable.TemplateParameter;
import fun.other.RawComponent;
import fun.other.RawHfsm;
import fun.other.Template;

public class ImplHfsmParser extends ImplBaseParser {
  public ImplHfsmParser(Scanner scanner) {
    super(scanner);
  }

  public static RawComponent parse(Scanner scanner, String name) {
    ImplHfsmParser parser = new ImplHfsmParser(scanner);
    return parser.parseImplementationHfsm(name);
  }

  // EBNF implementationComposition: "hfsm" "(" id ")" stateBody "end"
  private RawComponent parseImplementationHfsm(String name) {
    ElementInfo info = expect(TokenType.HFSM).getInfo();
    expect(TokenType.OPENPAREN);
    String initial = expect(TokenType.IDENTIFIER).getData();
    expect(TokenType.CLOSEPAREN);
    FuncProcedure entry = makeProc("_entry"); // FIXME get names from outside
    FuncProcedure exit = makeProc("_exit");// FIXME get names from outside
    StateComposite top = new StateComposite(info, "!top", new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit), new Reference(info, new DummyLinkTarget(info, initial)));
    top.item.add(entry);
    top.item.add(exit);
    parseStateBody(top);

    RawHfsm implHfsm = new RawHfsm(info, name, top);
    return implHfsm;
  }

  // EBNF stateBody: { entryCode | exitCode | varDeclBlock | funcDecl |
  // transitionDecl | state }
  private <T extends evl.data.component.hfsm.State> void parseStateBody(evl.data.component.hfsm.State state) {
    Block entryBody = state.entryFunc.link.body;
    Block exitBody = state.exitFunc.link.body;

    while (!consumeIfEqual(TokenType.END)) {
      switch (peek().getType()) {
        case ENTRY:
          entryBody.statements.add(parseEntryCode());
          break;
        case EXIT:
          exitBody.statements.add(parseExitCode());
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
              Function obj = parseStateDeclaration(id.getData());
              state.item.add(new Template(id.getInfo(), id.getData(), genpam, obj));
              break;
            case COLON:
              expect(TokenType.COLON);
              Evl inst = parseStateInstantiation(id.getData());
              state.item.add((StateContent) inst);
              break;
            default:
              state.item.add(parseTransition(id));
              break;
          }
      }
    }
  }

  private Evl parseStateInstantiation(String name) {
    switch (peek().getType()) {
      case STATE:
        return parseState(name);
      case CONST:
        evl.data.variable.ConstPrivate con = parseConstDef(ConstPrivate.class, name);
        expect(TokenType.SEMI);
        return con;
      case RESPONSE:
        return parseFuncDef(peek().getType(), name, false);
      default:
        evl.data.variable.StateVariable var = parseStateVardef(name);
        expect(TokenType.SEMI);
        return var;
    }
  }

  private Function parseStateDeclaration(String name) {
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

  // EBNF stateDecl: "state" ( ";" | ( [ "(" id ")" ] stateBody ) )
  private evl.data.component.hfsm.State parseState(String name) {
    ElementInfo info = expect(TokenType.STATE).getInfo();

    evl.data.component.hfsm.State state;
    FuncProcedure entry = makeProc("_entry"); // FIXME get names from outside
    FuncProcedure exit = makeProc("_exit");// FIXME get names from outside
    if (consumeIfEqual(TokenType.SEMI)) {
      state = new StateSimple(info, name, new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit));
      state.item.add(entry);
      state.item.add(exit);
    } else {
      if (consumeIfEqual(TokenType.OPENPAREN)) {
        String initial = expect(TokenType.IDENTIFIER).getData();
        expect(TokenType.CLOSEPAREN);
        state = new StateComposite(info, name, new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit), new Reference(info, new DummyLinkTarget(info, initial)));
      } else {
        state = new StateSimple(info, name, new SimpleRef<FuncProcedure>(info, entry), new SimpleRef<FuncProcedure>(info, exit));
      }
      state.item.add(entry);
      state.item.add(exit);

      parseStateBody(state);
    }
    return state;
  }

  static private FuncProcedure makeProc(String name) {
    return new FuncProcedure(ElementInfo.NO, name, new EvlList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), new Block(ElementInfo.NO));
  }

  // EBNF transition: nameRef "to" nameRef "by" transitionEvent [ "if" expr ] (
  // ";" | "do" block "end" )
  private Transition parseTransition(Token id) {
    Reference src = parseNameRef(id);
    ElementInfo info = expect(TokenType.TO).getInfo();
    Reference dst = parseNameRef();

    Transition ret = Transition.create(info);
    ret.src = src;
    ret.dst = dst;

    expect(TokenType.BY);
    parseTransitionEvent(ret);
    {
      Expression guard;
      if (consumeIfEqual(TokenType.IF)) {
        guard = expr().parse();
      } else {
        guard = new BoolValue(info, true);
      }
      ret.guard = guard;
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
      ret.body = body;
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
      ret.offset.add(new RefName(tok.getInfo(), tok.getData()));
    }

    return ret;
  }

  // EBNF transitionEvent: id vardeflist
  private void parseTransitionEvent(evl.data.component.hfsm.Transition ret) {
    Token tok = expect(TokenType.IDENTIFIER);
    Reference name = new Reference(tok.getInfo(), tok.getData());
    ret.eventFunc = name;
    ret.param.addAll(parseVardefList());
  }

}
