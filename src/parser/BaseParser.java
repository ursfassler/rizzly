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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.expression.ExpressionParser;
import parser.scanner.Scanner;
import parser.scanner.Token;
import parser.scanner.TokenType;
import util.Pair;
import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.function.Function;
import ast.data.function.FunctionFactory;
import ast.data.function.ret.FuncReturnNone;
import ast.data.function.ret.FuncReturnTuple;
import ast.data.function.ret.FuncReturnType;
import ast.data.reference.RefFactory;
import ast.data.statement.Block;
import ast.data.type.TypeRef;
import ast.data.type.special.AnyType;
import ast.data.variable.Constant;
import ast.data.variable.FuncVariable;
import ast.data.variable.StateVariable;
import ast.data.variable.TemplateParameter;
import ast.data.variable.Variable;
import ast.data.variable.VariableFactory;
import ast.knowledge.KnowFuncType;
import error.ErrorType;
import error.RError;

public class BaseParser extends Parser {

  public BaseParser(Scanner scanner) {
    super(scanner);
  }

  protected StatementParser stmt() {
    return new StatementParser(getScanner());
  }

  protected TypeParser type() {
    return new TypeParser(getScanner());
  }

  protected ExpressionParser expr() {
    return new ExpressionParser(getScanner());
  }

  static Set<TokenType> typeSet(TokenType type) {
    Set<TokenType> set = new HashSet<TokenType>();
    set.add(type);
    return set;
  }

  // TODO can we merge it with another function parser?
  // EBNF privateFunction: "function" vardeflist funcReturn ( block "end" | ";"
  // )
  protected Function parseFuncDef(TokenType type, String name, boolean neverHasBody) {
    Token next = next();
    if (!type.equals(next.getType())) {
      RError.err(ErrorType.Error, next.getInfo(), "Found " + next.getType() + ", extected " + type);
    }

    Class<? extends Function> cl = KnowFuncType.getClassOf(next.getType());

    AstList<FuncVariable> varlist = parseVardefList();

    ast.data.function.ret.FuncReturn retType;
    if (KnowFuncType.getWithRetval(cl)) {
      retType = parseFuncReturn();
    } else {
      retType = new FuncReturnNone(ElementInfo.NO);
    }

    Block body;
    if (!neverHasBody && KnowFuncType.getWithBody(cl)) {
      body = stmt().parseBlock();
      expect(TokenType.END);
    } else {
      expect(TokenType.SEMI);
      body = new Block(peek().getInfo());
    }

    return FunctionFactory.create(cl, next.getInfo(), name, varlist, retType, body);
  }

  // EBNF: funcReturn: [ ":" ( typeref | vardeflist ) ]
  protected ast.data.function.ret.FuncReturn parseFuncReturn() {
    ElementInfo info = peek().getInfo();
    if (consumeIfEqual(TokenType.COLON)) {
      if (peek().getType() == TokenType.OPENPAREN) {
        return new FuncReturnTuple(info, parseVardefList());
      } else {
        return new FuncReturnType(info, expr().parseRefType());
      }
    } else {
      return new FuncReturnNone(ElementInfo.NO);
    }
  }

  // EBNF vardefNoinit: id { "," id } ":" typeref
  protected <T extends Variable> AstList<T> parseVarDef(Class<T> kind) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));

    expect(TokenType.COLON);

    TypeRef type = expr().parseRefType();

    AstList<T> ret = new AstList<T>();
    for (int i = 0; i < names.size(); i++) {
      TypeRef ntype = Copy.copy(type);
      ret.add(VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype));
    }

    return ret;
  }

  // EBNF stateVardef: typeref "=" expr
  public StateVariable parseStateVardef(String name) {
    TypeRef type = expr().parseRefType();
    expect(TokenType.EQUAL);
    Expression init = expr().parse();
    return new StateVariable(type.getInfo(), name, type, init);
  }

  // EBNF objDef: id [ "{" vardef { ";" vardef } "}" ]
  protected Pair<Token, List<TemplateParameter>> parseObjDef() {
    Token name = expect(TokenType.IDENTIFIER);
    List<TemplateParameter> genpam;
    if (peek().getType() == TokenType.OPENCURLY) {
      genpam = parseGenericParam();
    } else {
      genpam = new ArrayList<TemplateParameter>();
    }
    return new Pair<Token, List<TemplateParameter>>(name, genpam);
  }

  // EBNF genericParam: [ "{" vardef { ";" vardef } "}" ]
  protected AstList<TemplateParameter> parseGenericParam() {
    AstList<TemplateParameter> ret = new AstList<TemplateParameter>();
    if (consumeIfEqual(TokenType.OPENCURLY)) {
      do {
        List<TemplateParameter> param = parseVarDef(TemplateParameter.class);
        ret.addAll(param);
      } while (consumeIfEqual(TokenType.SEMI));
      expect(TokenType.CLOSECURLY);
    }
    return ret;
  }

  // EBNF vardeflist: "(" [ vardef { ";" vardef } ] ")"
  protected AstList<FuncVariable> parseVardefList() {
    AstList<FuncVariable> res = new AstList<FuncVariable>();
    expect(TokenType.OPENPAREN);
    if (peek().getType() == TokenType.IDENTIFIER) {
      do {
        List<FuncVariable> list = stmt().parseVarDef(FuncVariable.class);
        res.addAll(list);
      } while (consumeIfEqual(TokenType.SEMI));
    }
    expect(TokenType.CLOSEPAREN);
    return res;
  }

  // EBNF constdef: "const" [ typeref ] "=" expr
  public <T extends Constant> T parseConstDef(Class<T> kind, String name) {
    ElementInfo info = expect(TokenType.CONST).getInfo();
    TypeRef type;
    if (peek().getType() != TokenType.EQUAL) {
      type = expr().parseRefType();
    } else {
      type = new TypeRef(info, RefFactory.create(info, AnyType.NAME));
    }
    expect(TokenType.EQUAL);
    ast.data.expression.Expression value = expr().parse();

    return VariableFactory.create(kind, info, name, type, value);
  }

}
