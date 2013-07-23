package fun.toevl;

import java.util.ArrayList;
import java.util.Map;

import evl.Evl;
import evl.expression.Expression;
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
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
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
    for (RefItem item : obj.getOffset()) {
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
    for (fun.expression.Expression item : obj.getValue()) {
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
    return new evl.expression.ArithmeticOp(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null), obj.getOp());
  }

  @Override
  protected Expression visitRelation(Relation obj, String param) {
    return new evl.expression.Relation(obj.getInfo(), (Expression) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null), obj.getOp());
  }

  @Override
  protected Expression visitUnaryExpression(UnaryExpression obj, String param) {
    return new evl.expression.UnaryExpression(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null), obj.getOp());
  }

}
