package parser;

import java.util.ArrayList;
import java.util.List;

import error.ErrorType;
import error.RError;
import evl.expression.ExpOp;
import evl.expression.RelOp;
import evl.expression.UnaryOp;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefCompcall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceUnlinked;

public class ExpressionParser extends Parser {

  public ExpressionParser(PeekReader<Token> scanner) {
    super(scanner);
  }

  // EBNF exprList: expr { "," expr }
  protected List<Expression> parseExprList() {
    List<Expression> ret = new ArrayList<Expression>();
    do {
      ret.add(parse());
    } while (consumeIfEqual(TokenType.COMMA));
    return ret;
  }

  // EBNF expr: shiftExpr [ relOp shiftExpr ]
  protected Expression parse() {
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

  // actually it has to be: ref: id { refName } [ refGeneric ] { refName | refIndex | refCall }
  // EBNF ref: id { refName | refIndex | refCall | refGeneric }
  protected Reference parseRef() {
    Token head = expect(TokenType.IDENTIFIER);
    Reference res = new ReferenceUnlinked(head.getInfo());
    res.getOffset().add(new RefName(head.getInfo(), head.getData()));

    while (true) {
      switch (peek().getType()) {
      case PERIOD:
        res.getOffset().add(parseRefName());
        break;
      case OPEN_ARRAY:
        res.getOffset().add(parseRefIndex());
        break;
      case OPENPAREN:
        res.getOffset().add(parseRefCall());
        break;
      case OPENCURLY:
        res.getOffset().add(parseRefGeneric());
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

  // EBNF relOp: "=" | "<>" | "<" | "<=" | ">" | ">="
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
    RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token");
    return null;
  }

  // EBNF factor: ref | number | string | arrayValue | "False" | "True" | "not" factor | "(" expr ")"
  private Expression parseFactor() {
    TokenType type = peek().getType();
    switch (type) {
    case IDENTIFIER: {
      return parseRef();
    }
/*    case STAR: {
      ElementInfo info = next().getInfo();
      Reference ref = new ReferenceUnlinked(info);
      ref.getOffset().add(new RefName(info, AnyType.NAME));
      return ref;
    }*/
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
    case OPEN_ARRAY: {
      return parseArrayValue();
    }
    case OPENPAREN: {
      expect(TokenType.OPENPAREN);
      Expression ret = parse();
      expect(TokenType.CLOSEPAREN);
      return ret;
    }
    default:
      RError.err(ErrorType.Fatal, peek().getInfo(), "Unexpected token: " + type);
    }
    return null;
  }

  // EBNF arrayValue: "[" exprList "]"
  private Expression parseArrayValue() {
    Token tok = expect(TokenType.OPEN_ARRAY);
    ArrayValue ret = new ArrayValue(tok.getInfo(), parseExprList());
    expect(TokenType.CLOSE_ARRAY);
    return ret;
  }

  // EBNF refName: "." id
  private RefName parseRefName() {
    Token tok = expect(TokenType.PERIOD);
    String name = expect(TokenType.IDENTIFIER).getData();
    return new RefName(tok.getInfo(), name);
  }

  // EBNF refIndex: "[" expr "]"
  private RefIndex parseRefIndex() {
    Token tok = expect(TokenType.OPEN_ARRAY);
    Expression expr = parse();
    expect(TokenType.CLOSE_ARRAY);
    return new RefIndex(tok.getInfo(), expr);
  }

  // EBNF refCall: "(" [ exprList ] ")"
  private RefCall parseRefCall() {
    Token tok = expect(TokenType.OPENPAREN);
    List<Expression> expr;
    if (peek().getType() == TokenType.CLOSEPAREN) {
      expr = new ArrayList<Expression>();
    } else {
      expr = parseExprList();
    }
    expect(TokenType.CLOSEPAREN);
    return new RefCall(tok.getInfo(), expr);
  }

  // EBNF refGeneric: "{" [ exprList ] "}"
  private RefCompcall parseRefGeneric() {
    Token tok = expect(TokenType.OPENCURLY);
    List<Expression> expr;
    if (peek().getType() == TokenType.CLOSECURLY) {
      expr = new ArrayList<Expression>();
    } else {
      expr = parseExprList();
    }
    expect(TokenType.CLOSECURLY);
    return new RefCompcall(tok.getInfo(), expr);
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
