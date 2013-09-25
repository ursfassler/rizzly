package fun.toevl;

import java.util.ArrayList;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.binop.And;
import evl.expression.binop.Div;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Minus;
import evl.expression.binop.Mod;
import evl.expression.binop.Mul;
import evl.expression.binop.Notequal;
import evl.expression.binop.Or;
import evl.expression.binop.Plus;
import evl.expression.binop.Shl;
import evl.expression.binop.Shr;
import evl.expression.reference.Reference;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArithmeticOp;
import fun.expression.ArrayValue;
import fun.expression.BoolValue;
import fun.expression.Number;
import fun.expression.Relation;
import fun.expression.StringValue;
import fun.expression.UnaryExpression;
import fun.expression.reference.RefItem;
import fun.expression.reference.ReferenceLinked;

public class FunToEvlExpr extends NullTraverser<Evl, String> {

  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlExpr(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected Evl visit(Fun obj, String param) {
    Evl cobj = (Evl) map.get(obj);
    if( cobj == null ) {
      cobj = super.visit(obj, param);
      assert ( cobj != null );
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Evl visitDefault(Fun obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------
  @Override
  protected Expression visitReferenceLinked(ReferenceLinked obj, String param) {
    ArrayList<evl.expression.reference.RefItem> ofs = new ArrayList<evl.expression.reference.RefItem>();
    for( RefItem item : obj.getOffset() ) {
      ofs.add((evl.expression.reference.RefItem) fta.traverse(item, null));
    }
    Reference ret = new evl.expression.reference.Reference(obj.getInfo(), (evl.other.Named) fta.traverse(obj.getLink(), null), ofs);
    return ret;
  }

  @Override
  protected Expression visitNumber(Number obj, String param) {
    return new evl.expression.Number(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Expression visitStringValue(StringValue obj, String param) {
    return new evl.expression.StringValue(obj.getInfo(), obj.getValue());
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, String param) {
    ArrayList<evl.expression.Expression> value = new ArrayList<evl.expression.Expression>();
    for( fun.expression.Expression item : obj.getValue() ) {
      value.add((evl.expression.Expression) fta.traverse(item, null));
    }
    return new evl.expression.ArrayValue(obj.getInfo(), value);
  }

  @Override
  protected Expression visitBoolValue(BoolValue obj, String param) {
    return new evl.expression.BoolValue(obj.getInfo(), obj.isValue());
  }

  @Override
  protected Expression visitArithmeticOp(ArithmeticOp obj, String param) {
    switch( obj.getOp() ) {
      case PLUS:
        return new Plus(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MINUS:
        return new Minus(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MUL:
        return new Mul(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case DIV:
        return new Div(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case MOD:
        return new Mod(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case AND:
        return new And(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case OR:
        return new Or(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case SHL:
        return new Shl(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case SHR:
        return new Shr(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }

  @Override
  protected Expression visitRelation(Relation obj, String param) {
    switch( obj.getOp() ) {
      case EQUAL:
        return new Equal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case NOT_EQUAL:
        return new Notequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case GREATER:
        return new Greater(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case GREATER_EQUEAL:
        return new Greaterequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case LESS:
        return new Less(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      case LESS_EQUAL:
        return new Lessequal(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, String param) {
    switch( obj.getOp() ) {
      case MINUS:
        return new evl.expression.unop.Uminus(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
      case NOT:
        return new evl.expression.unop.Not(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
      default:
        RError.err(ErrorType.Fatal, obj.getInfo(), "Unhandled case: " + obj.getOp());
    }
    return null;
  }
}
