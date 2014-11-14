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
import fun.expression.AnyValue;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.function.FuncHeader;
import fun.function.FunctionFactory;
import fun.knowledge.KnowFuncType;
import fun.other.FunList;
import fun.statement.Block;
import fun.type.base.AnyType;
import fun.type.base.VoidType;
import fun.variable.Constant;
import fun.variable.FuncVariable;
import fun.variable.TemplateParameter;
import fun.variable.Variable;
import fun.variable.VariableFactory;

public class BaseParser extends Parser {

  enum InitType {
    NoInit, CanInit, MustInit
  }

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
  // EBNF privateFunction: "function" vardeflist [ ":" typeref ] ( block "end" | ";" )
  protected FuncHeader parseFuncDef(TokenType type, String name, boolean neverHasBody) {
    Token next = next();
    if (!type.equals(next.getType())) {
      RError.err(ErrorType.Error, next.getInfo(), "Found " + next.getType() + ", extected " + type);
    }

    Class<? extends FuncHeader> cl = KnowFuncType.getClassOf(next.getType());

    FunList<FuncVariable> varlist = parseVardefList();

    Reference retType;
    if (KnowFuncType.getWithRetval(cl)) {
      expect(TokenType.COLON);
      retType = expr().parseRef();
    } else {
      retType = new Reference(ElementInfo.NO, VoidType.NAME);
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

  // EBNF vardefNoinit: id { "," id } ":" typeref
  // EBNF vardefCaninit: id { "," id } ":" typeref [ "=" exprList ]
  // EBNF vardefMustinit: id { "," id } ":" typeref "=" exprList
  protected <T extends Variable> FunList<T> parseVarDef(Class<T> kind, InitType init) {
    List<Token> names = new ArrayList<Token>();
    do {
      Token id = expect(TokenType.IDENTIFIER);
      names.add(id);
    } while (consumeIfEqual(TokenType.COMMA));

    expect(TokenType.COLON);
    Reference type = expr().parseRef();

    FunList<Expression> def = new FunList<Expression>();
    if ((init == InitType.MustInit) || ((init == InitType.CanInit) && (peek().getType() == TokenType.EQUAL))) {
      expect(TokenType.EQUAL);
      def = expr().parseExprList();
    } else {
      for (Token name : names) {
        def.add(new AnyValue(name.getInfo()));
      }
    }

    if (names.size() != def.size()) {
      RError.err(ErrorType.Error, names.get(0).getInfo(), "expected " + names.size() + " init values, got " + def.size());
      return null;
    }

    FunList<T> ret = new FunList<T>();
    for (int i = 0; i < names.size(); i++) {
      Reference ntype = Copy.copy(type);
      T var;
      if (init == InitType.NoInit) {
        var = VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype);
      } else {
        var = VariableFactory.create(kind, names.get(i).getInfo(), names.get(i).getData(), ntype, def.get(i));
      }
      ret.add(var);
    }

    return ret;
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
        List<TemplateParameter> param = parseVarDef(TemplateParameter.class, InitType.NoInit);
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
        List<FuncVariable> list = stmt().parseVarDef(FuncVariable.class, InitType.NoInit);
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

  // EBNF vardefNoinit: typeref
  // EBNF vardefCaninit: typeref [ "=" expr ]
  // EBNF vardefMustinit: typeref "=" expr
  public <T extends Variable> T parseVarDef2(Class<T> kind, String name, InitType init) {
    Reference type = expr().parseRef();
    Expression value;

    if ((init == InitType.MustInit) || ((init == InitType.CanInit) && (peek().getType() == TokenType.EQUAL))) {
      expect(TokenType.EQUAL);
      value = expr().parse();
    } else {
      value = new AnyValue(type.getInfo());
    }

    T var;
    if (init == InitType.NoInit) {
      var = VariableFactory.create(kind, type.getInfo(), name, type);
    } else {
      var = VariableFactory.create(kind, type.getInfo(), name, type, value);
    }

    return var;
  }
}
