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

import util.Pair;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.Copy;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.function.FuncReturn;
import fun.function.FuncReturnNone;
import fun.function.FuncReturnTuple;
import fun.function.FuncReturnType;
import fun.function.FunctionFactory;
import fun.knowledge.KnowFuncType;
import fun.other.FunList;
import fun.statement.Block;
import fun.type.base.AnyType;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.StateVariable;
import fun.variable.TemplateParameter;
import fun.variable.Variable;
import fun.variable.VariableFactory;

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
  // EBNF privateFunction: "function" vardeflist funcReturn ( block "end" | ";" )
  protected FuncHeader parseFuncDef(TokenType type, String name, boolean neverHasBody) {
    Token next = next();
    if (!type.equals(next.getType())) {
      RError.err(ErrorType.Error, next.getInfo(), "Found " + next.getType() + ", extected " + type);
    }

    Class<? extends FuncHeader> cl = KnowFuncType.getClassOf(next.getType());

    FunList<FuncVariable> varlist = parseVardefList();

    FuncReturn retType;
    if (KnowFuncType.getWithRetval(cl)) {
      retType = parseFuncReturn();
    } else {
      retType = new FuncReturnNone(ElementInfo.NO);
    }

    FuncHeader func;

    if (!neverHasBody && KnowFuncType.getWithBody(cl)) {
      Block body = stmt().parseBlock();
      expect(TokenType.END);
      func = FunctionFactory.create(cl, next.getInfo(), name, varlist, retType, body);
    } else {
      expect(TokenType.SEMI);
      func = FunctionFactory.create(cl, next.getInfo(), name, varlist, retType);
    }

    return func;
  }

  // EBNF: funcReturn: [ ":" ( typeref | vardeflist ) ]
  protected FuncReturn parseFuncReturn() {
    ElementInfo info = peek().getInfo();
    if (consumeIfEqual(TokenType.COLON)) {
      if (peek().getType() == TokenType.OPENPAREN) {
        return new FuncReturnTuple(info, parseVardefList());
      } else {
        return new FuncReturnType(info, expr().parseRef());
      }
    } else {
      return new FuncReturnNone(ElementInfo.NO);
    }
  }

  // EBNF vardefNoinit: id { "," id } ":" typeref
  protected <T extends Variable> FunList<T> parseVarDef(Class<T> kind) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));

    expect(TokenType.COLON);

    Reference type = expr().parseRef();

    FunList<T> ret = new FunList<T>();
    for (int i = 0; i < names.size(); i++) {
      Reference ntype = Copy.copy(type);
      ret.add(VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype));
    }

    return ret;
  }

  // EBNF stateVardef: typeref "=" expr
  public StateVariable parseStateVardef(String name) {
    Reference type = expr().parseRef();
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
  protected FunList<TemplateParameter> parseGenericParam() {
    FunList<TemplateParameter> ret = new FunList<TemplateParameter>();
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
  protected FunList<FuncVariable> parseVardefList() {
    FunList<FuncVariable> res = new FunList<FuncVariable>();
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
    Reference type;
    if (peek().getType() != TokenType.EQUAL) {
      type = expr().parseRef();
    } else {
      type = new Reference(info, AnyType.NAME);
    }
    expect(TokenType.EQUAL);
    Expression value = expr().parse();

    return VariableFactory.create(kind, info, name, type, value);
  }

}
