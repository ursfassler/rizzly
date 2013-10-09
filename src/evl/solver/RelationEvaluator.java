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
import evl.expression.binop.Notequal;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.type.base.NumSet;
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
    NumSet rt = (NumSet) param.getType().getRef();
    return new RangeValue(rt.getInfo(), rt.getNumbers().getLow(), rt.getNumbers().getHigh());
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

  private Expression makeRetLess(RangeValue leftRange,RangeValue rightRange, ElementInfo info) {
    BigInteger low = leftRange.getLow();
    BigInteger high = rightRange.getHigh().subtract(BigInteger.ONE);
    high = high.min(leftRange.getHigh());

    if (low.compareTo(high) > 0) {
      return new BoolValue(info, false); // TODO check
    }

    leftRange = new RangeValue(info, low, high);
    return leftRange;
  }

  private Expression makeRetLessEqual(RangeValue leftRange,RangeValue rightRange, ElementInfo info) {
    BigInteger low = leftRange.getLow();
    BigInteger high = rightRange.getHigh();
    high = high.min(leftRange.getHigh());

    if (low.compareTo(high) > 0) {
      return new BoolValue(info, false); // TODO check
    }

    leftRange = new RangeValue(info, low, high);
    return leftRange;
  }

  private Expression makeRetGreater(RangeValue leftRange,RangeValue rightRange, ElementInfo info) {
    if (leftRange.getLow().compareTo(rightRange.getHigh()) <= 0) {
      BigInteger low = rightRange.getHigh().add(BigInteger.ONE);
      if (leftRange.getHigh().compareTo(low) < 0) {
        return new BoolValue(info, false); // TODO check
      }
      return new RangeValue(info, low, leftRange.getHigh());
    }
    return leftRange;
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
      assert(false);
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
      return makeRetGreater(varr,rr,obj.getInfo());
    }

    throw new RuntimeException("reached unreachable code");
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
      return makeRetLess(varr,rr,obj.getInfo());
    }

    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitLessequal(Lessequal obj, Variable param) {
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
      return makeRetLessEqual(varr,rr,obj.getInfo());
    }

    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Expression visitEqual(Equal obj, Variable param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if ((left instanceof RangeValue) && !(right instanceof RangeValue)) {
      Expression t = left;
      left = right;
      right = t;
    }
    
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
  protected Expression visitNotequal(Notequal obj, Variable param) {
    Expression left = visit(obj.getLeft(), param);
    Expression right = visit(obj.getRight(), param);

    if ((left instanceof RangeValue) && !(right instanceof RangeValue)) {
      Expression t = left;
      left = right;
      right = t;
    }
    
    if (!(left instanceof RangeValue) && (right instanceof RangeValue)) {
      // TODO add solver
      assert (left instanceof Reference);
      assert (((Reference) left).getLink() == param);
      assert (((Reference) left).getOffset().isEmpty());
      RangeValue rr = (RangeValue) right;

      RangeValue varr = getRangeFromType(param);
      
      if( rr.getLow().equals(rr.getHigh()) ){
        BigInteger val = rr.getLow();
        BigInteger low = varr.getLow();
        BigInteger high = varr.getHigh();
        if( varr.getLow().equals(val) ){
          low = val.add(BigInteger.ONE);
        }
        if( varr.getHigh().equals(val) ){
          high = val.subtract(BigInteger.ONE);
        }
        if( low.compareTo(high) > 0 ){
          return new BoolValue(obj.getInfo(), false);
        } else {
          return new RangeValue(obj.getInfo(), low, high);
        }
      }
      
      // we can say nothing
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
