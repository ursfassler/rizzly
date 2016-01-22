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

package parser.expression;

import parser.Parser;
import parser.PeekNReader;
import parser.scanner.Token;
import parser.scanner.TokenType;
import ast.data.AstList;
import ast.data.component.ComponentReference;
import ast.data.component.hfsm.StateRef;
import ast.data.expression.Expression;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.binop.Relation;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.function.FunctionReference;
import ast.data.reference.LinkTarget;
import ast.data.reference.LinkedReferenceWithOffset;
import ast.data.reference.RefCall;
import ast.data.reference.RefFactory;
import ast.data.reference.RefIndex;
import ast.data.reference.RefName;
import ast.data.reference.RefTemplCall;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.template.ActualTemplateArgument;
import ast.data.type.TypeReference;
import ast.meta.MetaList;
import error.ErrorType;
import error.RError;

public class ExpressionParser extends Parser {

  public ExpressionParser(PeekNReader<Token> scanner) {
    super(scanner);
  }

  // EBNF exprList: expr { "," expr }
  protected AstList<Expression> parseExprList() {
    AstList<Expression> ret = new AstList<Expression>();
    do {
      ret.add(parse());
    } while (consumeIfEqual(TokenType.COMMA));
    return ret;
  }

  // EBNF tupleValue: "(" [ exprList ] ")"
  protected TupleValue parseTupleValue() {
    AstList<Expression> list;

    MetaList info = expect(TokenType.OPENPAREN).getMetadata();
    if (peek().getType() != TokenType.CLOSEPAREN) {
      list = parseExprList();
    } else {
      list = new AstList<Expression>();
    }
    expect(TokenType.CLOSEPAREN);

    TupleValue ret = new TupleValue(list);
    ret.metadata().add(info);
    return ret;
  }

  // EBNF parseNamedElementsValue: "[" [ assExpr { "," assExpr } ] "]"
  protected ast.data.expression.value.NamedElementsValue parseNamedElementsValue() {
    AstList<NamedValue> list = new AstList<NamedValue>();

    MetaList info = expect(TokenType.OPENBRACKETS).getMetadata();
    if (peek().getType() != TokenType.CLOSEBRACKETS) {
      do {
        list.add(parseAssExpr());
      } while (consumeIfEqual(TokenType.COMMA));
    }
    expect(TokenType.CLOSEBRACKETS);

    NamedElementsValue ret = new NamedElementsValue(list);
    ret.metadata().add(info);
    return ret;
  }

  // EBNF assExpr: relExpr | ( id ":=" relExpr )
  protected NamedValue parseAssExpr() {
    Expression expr = parseRelExpr();
    if (consumeIfEqual(TokenType.BECOMES)) {
      if (!(expr instanceof ReferenceExpression)) {
        RError.err(ErrorType.Error, "expected identifier for assignment", expr.metadata());
        return null;
      }
      LinkedReferenceWithOffset ref = ((ReferenceExpression) expr).reference;
      if (!ref.getOffset().isEmpty()) {
        RError.err(ErrorType.Error, "expected identifier for assignment", expr.metadata());
        return null;
      }
      Expression value = parseRelExpr();
      return new NamedValue(expr.metadata(), ((LinkTarget) ref.getLink()).getName(), value);
    } else {
      return new NamedValue(expr.metadata(), null, expr);
    }
  }

  // EBNF relExpr: shiftExpr [ relOp shiftExpr ]
  protected Expression parseRelExpr() {
    Expression expr = parseShiftExpr();
    if (isRelOp()) {
      Token tok = peek();
      RelOp op = parseRelOp();
      Expression right = parseShiftExpr();
      Relation relation = RelationFactory.create(expr, right, op);
      relation.metadata().add(tok.getMetadata());
      return relation;
    } else {
      return expr;
    }
  }

  // EBNF expr: relExpr
  public Expression parse() {
    return parseRelExpr();
  }

  // actually it has to be: ref: id { refName } [ refGeneric ] { refName |
  // refCall }
  // EBNF ref: id { refName | refCall | refIndex | refGeneric }
  public LinkedReferenceWithOffset_Implementation parseRef() {
    Token head = expect(TokenType.IDENTIFIER);
    LinkedReferenceWithOffset_Implementation res = RefFactory.full(head.getMetadata(), head.getData());

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

  public ReferenceExpression parseRefExpr() {
    LinkedReferenceWithOffset_Implementation ref = parseRef();
    ReferenceExpression ret = new ReferenceExpression(ref);
    ret.metadata().add(ref.metadata());
    return ret;
  }

  public TypeReference parseRefType() {
    LinkedReferenceWithOffset_Implementation ref = parseRef();
    TypeReference typeReference = new TypeReference(ref);
    typeReference.metadata().add(ref.metadata());
    return typeReference;
  }

  public FunctionReference parseRefFunc() {
    LinkedReferenceWithOffset_Implementation ref = parseRef();
    return new FunctionReference(ref.metadata(), ref);
  }

  public StateRef parseRefState() {
    LinkedReferenceWithOffset_Implementation ref = parseRef();
    return new StateRef(ref.metadata(), ref);
  }

  public ComponentReference parseRefComp() {
    LinkedReferenceWithOffset_Implementation ref = parseRef();
    return new ComponentReference(ref.metadata(), ref);
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
        RError.err(ErrorType.Error, "unexpected token: " + tok, tok.getMetadata());
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
      Expression ret = ArithmeticOpFactory.create(expr, right, op);
      ret.metadata().add(tok.getMetadata());
      return ret;
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
        RError.err(ErrorType.Error, "unexpected token: " + tok, tok.getMetadata());
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
      term = new Uminus(term);
      term.metadata().add(tok.getMetadata());
    }
    while (isAddOp()) {
      Token ntok = peek();
      ExpOp op = parseAddOp();
      Expression sterm = parseTerm();
      term = ArithmeticOpFactory.create(term, sterm, op);
      term.metadata().add(tok.getMetadata());
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
      factor = ArithmeticOpFactory.create(factor, sterm, op);
      factor.metadata().add(ntok.getMetadata());
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
    RError.err(ErrorType.Fatal, "Unexpected token", peek().getMetadata());
    return null;
  }

  // EBNF factor: ref | number | string | arrayValue | "False" | "True" | "not"
  // factor | tupleValue | namedElementsValue
  private Expression parseFactor() {
    TokenType type = peek().getType();
    switch (type) {
      case IDENTIFIER: {
        return parseRefExpr();
      }
      /*
       * case STAR: { ElementInfo info = next().getInfo(); Reference ref = new ReferenceUnlinked(info);
       * ref.getOffset().add(new RefName(info, AnyType.NAME)); return ref; }
       */
      case NUMBER: {
        Token tok = expect(TokenType.NUMBER);
        Expression ret = new NumberValue(tok.getNum());
        ret.metadata().add(tok.getMetadata());
        return ret;
      }
      case STRING: {
        Token tok = expect(TokenType.STRING);
        Expression ret = new StringValue(tok.getData());
        ret.metadata().add(tok.getMetadata());
        return ret;
      }
      case FALSE: {
        MetaList pos = next().getMetadata();
        Expression ret = new BooleanValue(false);
        ret.metadata().add(pos);
        return ret;
      }
      case TRUE: {
        MetaList pos = next().getMetadata();
        Expression ret = new BooleanValue(true);
        ret.metadata().add(pos);
        return ret;
      }
      case NOT: {
        MetaList pos = next().getMetadata();
        Expression ret = new Not(parseFactor());
        ret.metadata().add(pos);
        return ret;
      }
      case OPENPAREN: {
        return parseTupleValue();
      }
      case OPENBRACKETS: {
        return parseNamedElementsValue();
      }
      default:
        RError.err(ErrorType.Fatal, "Unexpected token: " + type, peek().getMetadata());
    }
    return null;
  }

  // EBNF refName: "." id
  private RefName parseRefName() {
    Token tok = expect(TokenType.PERIOD);
    String name = expect(TokenType.IDENTIFIER).getData();
    return new RefName(tok.getMetadata(), name);
  }

  // EBNF refCall: tupleValue
  private RefCall parseRefCall() {
    TupleValue arg = parseTupleValue();
    RefCall call = new RefCall(arg);
    call.metadata().add(arg.metadata());
    return call;
  }

  // EBNF refGeneric: "{" [ exprList ] "}"
  private RefTemplCall parseRefGeneric() {
    Token tok = expect(TokenType.OPENCURLY);
    AstList<ActualTemplateArgument> expr = new AstList<ActualTemplateArgument>();
    if (peek().getType() != TokenType.CLOSECURLY) {
      expr.addAll(parseExprList());
    }
    expect(TokenType.CLOSECURLY);
    return new RefTemplCall(tok.getMetadata(), expr);
  }

  // EBNF refIndex: "[" expr "]"
  private ast.data.reference.RefIndex parseRefIndex() {
    Token tok = expect(TokenType.OPENBRACKETS);
    Expression index = parse();
    expect(TokenType.CLOSEBRACKETS);
    return new RefIndex(tok.getMetadata(), index);
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
    RError.err(ErrorType.Fatal, "Unexpected token", peek().getMetadata());
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
