package evl.solver;

import java.math.BigInteger;

import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.AnyValue;
import evl.expression.BoolValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.RangeValue;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.type.base.Range;
import evl.variable.Variable;

public class RelationEvaluator extends NullTraverser<Expression, Variable> {
  private final static RelationEvaluator INSTANCE = new RelationEvaluator();

  public static Expression eval(Relation eq, Variable var) {
    return INSTANCE.traverse(eq, var);
  }

  @Override
  protected Expression visitDefault(Evl obj, Variable param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  static private RangeValue getRangeFromType(Variable param) {
    Range rt = (Range) param.getType().getRef();
    return new RangeValue(rt.getInfo(), rt.getLow(), rt.getHigh());
  }

  @Override
  protected Expression visitGreater(Greater obj, Variable param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if ((left instanceof RangeValue) && (right instanceof RangeValue)) {
      return greater(obj.getInfo(), (RangeValue) left, (RangeValue) right);
    }

    if (!(left instanceof RangeValue) && !(right instanceof RangeValue)) {
      throw new RuntimeException("not yet implemented");
    }

    if ((left instanceof RangeValue) && !(right instanceof RangeValue)) {
      // TODO add solver
      assert (right instanceof Reference);
      assert (((Reference) right).getLink() == param);
      assert (((Reference) right).getOffset().isEmpty());
      // TODO check ranges
      RangeValue varr = getRangeFromType(param);
      return new Lessequal(obj.getInfo(), right, left);
    }

    if (!(left instanceof RangeValue) && (right instanceof RangeValue)) {
      // TODO add solver
      assert (left instanceof Reference);
      assert (((Reference) left).getLink() == param);
      assert (((Reference) left).getOffset().isEmpty());
      RangeValue rr = (RangeValue) right;
      // TODO check ranges
      RangeValue varr = getRangeFromType(param);
      if (varr.getLow().compareTo(rr.getHigh()) <= 0) {
        BigInteger low = rr.getHigh().add(BigInteger.ONE);
        if (varr.getHigh().compareTo(low) < 0) {
          return new BoolValue(obj.getInfo(), false); // TODO check
        }
        varr = new RangeValue(obj.getInfo(), low, varr.getHigh());
      }
      return varr;
    }

    throw new RuntimeException("reached unreachable code");
  }

  private Expression greater(ElementInfo info, RangeValue left, RangeValue right) {
    if (left.getLow().compareTo(right.getHigh()) > 0) {
      return new BoolValue(info, true); // TODO check
    }
    if (left.getHigh().compareTo(right.getLow()) <= 0) {
      return new BoolValue(info, false);// TODO check
    }
    return new AnyValue(info); // TODO check
  }

  @Override
  protected Expression visitLess(Less obj, Variable param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if (!(left instanceof RangeValue) && (right instanceof RangeValue)) {
      // TODO add solver
      assert (left instanceof Reference);
      assert (((Reference) left).getLink() == param);
      assert (((Reference) left).getOffset().isEmpty());
      RangeValue rr = (RangeValue) right;
      // TODO check ranges
      RangeValue varr = getRangeFromType(param);

      BigInteger low = varr.getLow();
      BigInteger high = rr.getHigh().subtract(BigInteger.ONE);
      high = high.min(varr.getHigh());

      if (low.compareTo(high) > 0) {
        return new BoolValue(obj.getInfo(), false); // TODO check
      }

      varr = new RangeValue(obj.getInfo(), low, high);

      return varr;
    }

    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitEqual(Equal obj, Variable param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if (!(left instanceof RangeValue) && (right instanceof RangeValue)) {
      // TODO add solver
      assert (left instanceof Reference);
      assert (((Reference) left).getLink() == param);
      assert (((Reference) left).getOffset().isEmpty());
      RangeValue rr = (RangeValue) right;
      // TODO check ranges
      RangeValue varr = getRangeFromType(param);

      BigInteger low = rr.getLow().max(varr.getLow());
      BigInteger high = rr.getHigh().min(varr.getHigh());

      varr = new RangeValue(obj.getInfo(), low, high);
      return varr;
    }

    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitReference(Reference obj, Variable param) {
    if (obj.getLink() == param) {
      assert (obj.getOffset().isEmpty());
      return new Reference(obj.getInfo(), param);
    }
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitNumber(Number obj, Variable param) {
    return new RangeValue(obj.getInfo(), obj.getValue(), obj.getValue());
  }

}
