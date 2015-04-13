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

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.copy.Copy;
import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.function.Function;
import evl.data.function.FunctionFactory;
import evl.data.function.ret.FuncReturnNone;
import evl.data.function.ret.FuncReturnTuple;
import evl.data.function.ret.FuncReturnType;
import evl.data.statement.Block;
import evl.data.variable.Constant;
import evl.data.variable.FuncVariable;
import evl.data.variable.StateVariable;
import evl.data.variable.TemplateParameter;
import evl.data.variable.Variable;
import evl.knowledge.KnowFuncType;
import fun.VariableFactory;

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

    EvlList<FuncVariable> varlist = parseVardefList();

    evl.data.function.ret.FuncReturn retType;
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
  protected evl.data.function.ret.FuncReturn parseFuncReturn() {
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
  protected <T extends Variable> EvlList<T> parseVarDef(Class<T> kind) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));

    expect(TokenType.COLON);

    Reference type = expr().parseRef();

    EvlList<T> ret = new EvlList<T>();
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
  protected EvlList<TemplateParameter> parseGenericParam() {
    EvlList<TemplateParameter> ret = new EvlList<TemplateParameter>();
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
  protected EvlList<FuncVariable> parseVardefList() {
    EvlList<FuncVariable> res = new EvlList<FuncVariable>();
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
    evl.data.expression.reference.Reference type;
    if (peek().getType() != TokenType.EQUAL) {
      type = expr().parseRef();
    } else {
      type = new Reference(info, evl.data.type.special.AnyType.NAME);
    }
    expect(TokenType.EQUAL);
    evl.data.expression.Expression value = expr().parse();

    return VariableFactory.create(kind, info, name, type, value);
  }

}
