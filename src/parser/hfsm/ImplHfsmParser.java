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

package parser.hfsm;

import java.util.ArrayList;
import java.util.List;

import parser.ImplBaseParser;
import parser.PeekNReader;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.ElementInfo;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.StateRef;
import ast.data.component.hfsm.StateSimple;
import ast.data.component.hfsm.Transition;
import ast.data.expression.Expression;
import ast.data.expression.value.BoolValue;
import ast.data.function.FuncRef;
import ast.data.function.FuncRefFactory;
import ast.data.function.Function;
import ast.data.function.header.FuncProcedure;
import ast.data.function.ret.FuncReturnNone;
import ast.data.raw.RawComponent;
import ast.data.raw.RawHfsm;
import ast.data.reference.RefFactory;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.template.Template;
import ast.data.variable.ConstPrivate;
import ast.data.variable.FuncVariable;
import ast.data.variable.TemplateParameter;
import error.ErrorType;
import error.RError;

public class ImplHfsmParser extends ImplBaseParser {
  final private StateReferenceParser stateRef;

  public ImplHfsmParser(PeekNReader<Token> scanner) {
    super(scanner);
    stateRef = new StateReferenceParser(scanner, RError.instance());
  }

  public static RawComponent parse(PeekNReader<Token> scanner, String name) {
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
    StateComposite top = new StateComposite(info, "!top", FuncRefFactory.create(info, entry), FuncRefFactory.create(info, exit), new StateRef(info, RefFactory.create(info, initial)));
    top.item.add(entry);
    top.item.add(exit);
    parseStateBody(top);

    RawHfsm implHfsm = new RawHfsm(info, name, top);
    return implHfsm;
  }

  // EBNF stateBody: { entryCode | exitCode | varDeclBlock | funcDecl |
  // transitionDecl | state }
  private <T extends State> void parseStateBody(State state) {
    Block entryBody = state.entryFunc.getTarget().body;
    Block exitBody = state.exitFunc.getTarget().body;

    while (!consumeIfEqual(TokenType.END)) {
      switch (peek().getType()) {
        case ENTRY:
          entryBody.statements.add(parseEntryCode());
          break;
        case EXIT:
          exitBody.statements.add(parseExitCode());
          break;
        case IDENTIFIER:
          state.item.add(parseDeclOrInstOrTrans(state));
          break;
        default:
          wrongToken(TokenType.IDENTIFIER);
      }
    }
  }

  private StateContent parseDeclOrInstOrTrans(State state) {
    if (peek(0).getType() != TokenType.IDENTIFIER) {
      wrongToken(TokenType.IDENTIFIER);
      return null;
    }

    switch (peek(1).getType()) {
      case EQUAL: {
        Token id = next();
        expect(TokenType.EQUAL);
        List<TemplateParameter> genpam;
        if (peek().getType() == TokenType.OPENCURLY) {
          genpam = parseGenericParam();
        } else {
          genpam = new ArrayList<TemplateParameter>();
        }
        Function obj = parseStateDeclaration(id.getData());
        return new Template(id.getInfo(), id.getData(), genpam, obj);
      }
      case COLON: {
        Token id = next();
        expect(TokenType.COLON);
        Ast inst = parseStateInstantiation(id.getData());
        return (StateContent) inst;
      }
      default: {
        return parseTransition();
      }
    }
  }

  private Ast parseStateInstantiation(String name) {
    switch (peek().getType()) {
      case STATE:
        return parseState(name);
      case CONST:
        ast.data.variable.ConstPrivate con = parseConstDef(ConstPrivate.class, name);
        expect(TokenType.SEMI);
        return con;
      case RESPONSE:
        return parseFuncDef(peek().getType(), name, false);
      default:
        ast.data.variable.StateVariable var = parseStateVardef(name);
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
  private State parseState(String name) {
    ElementInfo info = expect(TokenType.STATE).getInfo();

    ast.data.component.hfsm.State state;
    FuncProcedure entry = makeProc("_entry"); // FIXME get names from outside
    FuncProcedure exit = makeProc("_exit");// FIXME get names from outside
    if (consumeIfEqual(TokenType.SEMI)) {
      state = new StateSimple(info, name, FuncRefFactory.create(info, entry), FuncRefFactory.create(info, exit));
      state.item.add(entry);
      state.item.add(exit);
    } else {
      if (consumeIfEqual(TokenType.OPENPAREN)) {
        String initial = expect(TokenType.IDENTIFIER).getData();
        expect(TokenType.CLOSEPAREN);
        state = new StateComposite(info, name, FuncRefFactory.create(info, entry), FuncRefFactory.create(info, exit), new StateRef(info, RefFactory.create(info, initial)));
      } else {
        state = new StateSimple(info, name, FuncRefFactory.create(info, entry), FuncRefFactory.create(info, exit));
      }
      state.item.add(entry);
      state.item.add(exit);

      parseStateBody(state);
    }
    return state;
  }

  static private FuncProcedure makeProc(String name) {
    return new FuncProcedure(ElementInfo.NO, name, new AstList<FuncVariable>(), new FuncReturnNone(ElementInfo.NO), new Block(ElementInfo.NO));
  }

  // EBNF transition: stateRef "to" stateRef "by" transitionEvent [ "if" expr ] (
  // ";" | "do" block "end" )
  private Transition parseTransition() {
    StateRef src = stateRef.parse();
    ElementInfo info = expect(TokenType.TO).getInfo();
    StateRef dst = stateRef.parse();

    expect(TokenType.BY);

    Token tok = expect(TokenType.IDENTIFIER);
    Reference name = RefFactory.full(tok.getInfo(), tok.getData());
    FuncRef eventFunc = new FuncRef(name.getInfo(), name);

    AstList<FuncVariable> param = parseVardefList();

    Expression guard;
    if (consumeIfEqual(TokenType.IF)) {
      guard = expr().parse();
    } else {
      guard = new BoolValue(info, true);
    }

    Block body;
    if (consumeIfEqual(TokenType.SEMI)) {
      body = new Block(info);
    } else {
      expect(TokenType.DO);
      body = stmt().parseBlock();
      expect(TokenType.END);
    }

    return new Transition(info, src, dst, eventFunc, guard, param, body);
  }

}
