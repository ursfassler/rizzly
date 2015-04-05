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

import parser.scanner.Scanner;
import parser.scanner.Token;
import parser.scanner.TokenType;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.ArithmeticOp;
import fun.expression.BoolValue;
import fun.expression.ExpOp;
import fun.expression.Expression;
import fun.expression.NamedElementsValue;
import fun.expression.NamedValue;
import fun.expression.Number;
import fun.expression.RelOp;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.UnaryExpression;
import fun.expression.UnaryOp;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.Reference;
import fun.other.ActualTemplateArgument;
import fun.other.FunList;

public class ExpressionParser extends Parser {

  public ExpressionParser(Scanner scanner) {
    super(scanner);
  }

  // EBNF exprList: expr { "," expr }
  protected FunList<Expression> parseExprList() {
    FunList<Expression> ret = new FunList<Expression>();
    do {
      ret.add(parse());
    } while (consumeIfEqual(TokenType.COMMA));
    return ret;
  }

  // EBNF tupleValue: "(" [ exprList ] ")"
  protected TupleValue parseTupleValue() {
    FunList<Expression> list;

    ElementInfo info = expect(TokenType.OPENPAREN).getInfo();
    if (peek().getType() != TokenType.CLOSEPAREN) {
      list = parseExprList();
    } else {
      list = new FunList<Expression>();
    }
    expect(TokenType.CLOSEPAREN);

    return new TupleValue(info, list);
  }

  // EBNF parseNamedElementsValue: "[" [ assExpr { "," assExpr } ] "]"
  protected NamedElementsValue parseNamedElementsValue() {
    FunList<NamedValue> list = new FunList<NamedValue>();

    ElementInfo info = expect(TokenType.OPENBRACKETS).getInfo();
    if (peek().getType() != TokenType.CLOSEBRACKETS) {
      do {
        list.add(parseAssExpr());
      } while (consumeIfEqual(TokenType.COMMA));
    }
    expect(TokenType.CLOSEBRACKETS);

    return new NamedElementsValue(info, list);
  }

  // EBNF assExpr: relExpr | ( id ":=" relExpr )
  protected NamedValue parseAssExpr() {
    Expression expr = parseRelExpr();
    if (consumeIfEqual(TokenType.BECOMES)) {
      if (!(expr instanceof Reference) || !((Reference) expr).getOffset().isEmpty()) {
        RError.err(ErrorType.Error, expr.getInfo(), "expected identifier for assignment");
        return null;
      }
      Expression value = parseRelExpr();
      return new NamedValue(expr.getInfo(), ((DummyLinkTarget) ((Reference) expr).getLink()).getName(), value);
    } else {
      return new NamedValue(expr.getInfo(), null, expr);
    }
  }

  // EBNF relExpr: shiftExpr [ relOp shiftExpr ]
  protected Expression parseRelExpr() {
    Expression expr = parseShiftExpr();
    if (isRelOp()) {
      Token tok = peek();
      RelOp op = parseRelOp();
      Expression right = parseShiftExpr();
      return new Relation(tok.getInfo(), expr, right, op);
    } else {
      return expr;
    }
  }

  // EBNF expr: relExpr
  protected Expression parse() {
    return parseRelExpr();
  }

  // actually it has to be: ref: id { refName } [ refGeneric ] { refName | refCall }
  // EBNF ref: id { refName | refCall | refIndex | refGeneric }
  protected Reference parseRef() {
    Token head = expect(TokenType.IDENTIFIER);
    Reference res = new Reference(head.getInfo(), head.getData());

    while (true) {
      switch (peek().getType()) {
        case PERIOD:
          res.getOffset().add(parseRefName());
          break;
        case OPENPAREN:
          res.getOffset().add(parseRefCall());
          break;
        case OPENCURLY:
          res.getOffset().add(parseRefGeneric());
          break;
        case OPENBRACKETS:
          res.getOffset().add(parseRefIndex());
          break;
        default:
          return res;
      }
    }
  }

  // EBNF shiftOp: "shr" | "shl"
  private ExpOp parseShiftOp() {
    Token tok = next();
    switch (tok.getType()) {
      case SHR:
        return ExpOp.SHR;
      case SHL:
        return ExpOp.SHL;
      default:
        RError.err(ErrorType.Error, tok.getInfo(), "unexpected token: " + tok);
        return null;
    }
  }

  // EBNF shiftExpr: simpleExpr [ shiftOp simpleExpr ]
  private Expression parseShiftExpr() {
    Expression expr = parseSimpleExpr();
    if (isShiftOp()) {
      Token tok = peek();
      ExpOp op = parseShiftOp();
      Expression right = parseSimpleExpr();
      return new ArithmeticOp(tok.getInfo(), expr, right, op);
    } else {
      return expr;
    }
  }

  // EBNF relOp: "=" | "<>" | "<" | "<=" | ">" | ">=" | "is"
  private RelOp parseRelOp() {
    Token tok = next();
    switch (tok.getType()) {
      case EQUAL:
        return RelOp.EQUAL;
      case NEQ:
        return RelOp.NOT_EQUAL;
      case LOWER:
        return RelOp.LESS;
      case LEQ:
        return RelOp.LESS_EQUAL;
      case GREATER:
        return RelOp.GREATER;
      case GEQ:
        return RelOp.GREATER_EQUEAL;
      case IS:
        return RelOp.IS;
      default:
        RError.err(ErrorType.Error, tok.getInfo(), "unexpected token: " + tok);
        return null;
    }
  }

  // EBNF simpleExpr: [ sign ] term { addOp term }
  private Expression parseSimpleExpr() {
    boolean negate = false;
    Token tok = peek();
    switch (tok.getType()) {
      case PLUS:
        next();
        break;
      case MINUS:
        next();
        negate = true;
        break;
      default:
        break;
    }
    Expression term = parseTerm();
    if (negate) {
      term = new UnaryExpression(tok.getInfo(), term, UnaryOp.MINUS);
    }
    while (isAddOp()) {
      Token ntok = peek();
      ExpOp op = parseAddOp();
      Expression sterm = parseTerm();
      term = new ArithmeticOp(ntok.getInfo(), term, sterm, op);
    }
    return term;
  }

  // EBNF term: factor { mulOp factor }
  private Expression parseTerm() {
    Expression factor = parseFactor();
    while (isMulOp()) {
      Token ntok = peek();
      ExpOp op = parseMulOp();
      Expression sterm = parseFactor();
      factor = new ArithmeticOp(ntok.getInfo(), factor, sterm, op);
    }
    return factor;
  }

  // EBNF mulOp: "*" | "/" | "mod" | "and"
  private ExpOp parseMulOp() {
    if (consumeIfEqual(TokenType.STAR)) {
      return ExpOp.MUL;
    }
    if (consumeIfEqual(TokenType.DIV)) {
      return ExpOp.DIV;
    }
    if (consumeIfEqual(TokenType.MOD)) {
      return ExpOp.MOD;
    }
    if (consumeIfEqual(TokenType.AND)) {
      return ExpOp.AND;
    }
    if (consumeIfEqual(TokenType.XOR)) {
      return ExpOp.XOR;
    }
    RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token");
    return null;
  }

  // EBNF factor: ref | number | string | arrayValue | "False" | "True" | "not" factor | tupleValue | namedElementsValue
  private Expression parseFactor() {
    TokenType type = peek().getType();
    switch (type) {
      case IDENTIFIER: {
        return parseRef();
      }
      /*
       * case STAR: { ElementInfo info = next().getInfo(); Reference ref = new ReferenceUnlinked(info);
       * ref.getOffset().add(new RefName(info, AnyType.NAME)); return ref; }
       */
      case NUMBER: {
        Token tok = expect(TokenType.NUMBER);
        return new Number(tok.getInfo(), tok.getNum());
      }
      case STRING: {
        Token tok = expect(TokenType.STRING);
        return new StringValue(tok.getInfo(), tok.getData());
      }
      case FALSE: {
        return new BoolValue(next().getInfo(), false);
      }
      case TRUE: {
        return new BoolValue(next().getInfo(), true);
      }
      case NOT: {
        return new UnaryExpression(next().getInfo(), parseFactor(), UnaryOp.NOT);
      }
      case OPENPAREN: {
        return parseTupleValue();
      }
      case OPENBRACKETS: {
        return parseNamedElementsValue();
      }
      default:
        RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token: " + type);
    }
    return null;
  }

  // EBNF refName: "." id
  private RefName parseRefName() {
    Token tok = expect(TokenType.PERIOD);
    String name = expect(TokenType.IDENTIFIER).getData();
    return new RefName(tok.getInfo(), name);
  }

  // EBNF refCall: tupleValue
  private RefCall parseRefCall() {
    TupleValue arg = parseTupleValue();
    return new RefCall(arg.getInfo(), arg);
  }

  // EBNF refGeneric: "{" [ exprList ] "}"
  private RefTemplCall parseRefGeneric() {
    Token tok = expect(TokenType.OPENCURLY);
    FunList<ActualTemplateArgument> expr = new FunList<ActualTemplateArgument>();
    if (peek().getType() != TokenType.CLOSECURLY) {
      expr.addAll(parseExprList());
    }
    expect(TokenType.CLOSECURLY);
    return new RefTemplCall(tok.getInfo(), expr);
  }

  // EBNF refIndex: "[" expr "]"
  private RefIndex parseRefIndex() {
    Token tok = expect(TokenType.OPENBRACKETS);
    Expression index = parse();
    expect(TokenType.CLOSEBRACKETS);
    return new RefIndex(tok.getInfo(), index);
  }

  // EBNF addOp: "+" | "-" | "or"
  private ExpOp parseAddOp() {
    if (consumeIfEqual(TokenType.PLUS)) {
      return ExpOp.PLUS;
    }
    if (consumeIfEqual(TokenType.MINUS)) {
      return ExpOp.MINUS;
    }
    if (consumeIfEqual(TokenType.OR)) {
      return ExpOp.OR;
    }
    RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token");
    return null;
  }

  // --- Helper ----

  private boolean isRelOp() {
    switch (peek().getType()) {
      case EQUAL:
      case NEQ:
      case LOWER:
      case LEQ:
      case GREATER:
      case GEQ:
      case IS:
        return true;
      default:
        return false;
    }
  }

  private boolean isAddOp() {
    switch (peek().getType()) {
      case PLUS:
      case MINUS:
      case OR:
        return true;
      default:
        return false;
    }
  }

  private boolean isMulOp() {
    switch (peek().getType()) {
      case STAR:
      case DIV:
      case MOD:
      case AND:
      case XOR:
        return true;
      default:
        return false;
    }
  }

  private boolean isShiftOp() {
    switch (peek().getType()) {
      case SHL:
      case SHR:
        return true;
      default:
        return false;
    }
  }

}
