package evl.solver;

import java.math.BigInteger;

import util.NumberSet;
import util.NumberSetOperator;
import util.Range;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.RangeValue;
import evl.expression.binop.Equal;
import evl.expression.binop.Greater;
import evl.expression.binop.Greaterequal;
import evl.expression.binop.Less;
import evl.expression.binop.Lessequal;
import evl.expression.binop.Notequal;
import evl.expression.binop.Relation;
import evl.expression.reference.Reference;
import evl.type.base.NumSet;
import evl.variable.Variable;

public class RelationEvaluator extends NullTraverser<NumberSet, NumberSet> {
  private final static RelationEvaluator INSTANCE = new RelationEvaluator();

  public static NumberSet eval(Relation eq, Variable var) {
    assert (eq.getLeft() instanceof Reference);
    assert (((Reference) eq.getLeft()).getLink() == var);
    assert (eq.getRight() instanceof RangeValue);
    assert (var.getType().getRef() instanceof NumSet);
    NumSet ns = (NumSet) var.getType().getRef();

    NumberSet retset = INSTANCE.traverse(eq, ns.getNumbers());

    return retset;
  }

  @Override
  protected NumberSet visitDefault(Evl obj, NumberSet param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  private NumberSet getRange(Relation obj) {
    return ((RangeValue) obj.getRight()).getValues();
  }

  @Override
  protected NumberSet visitGreater(Greater obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    NumberSet set = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = right.getLow().add(BigInteger.ONE);
        low = low.max(left.getLow());
        BigInteger high = left.getHigh();
        if (low.compareTo(high) > 0) {
          return null;
        } else {
          return new Range(low, high);
        }
      }
    }, param, rvalues);
    return set;
  }

  @Override
  protected NumberSet visitGreaterequal(Greaterequal obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    NumberSet set = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = right.getLow();
        low = low.max(left.getLow());
        BigInteger high = left.getHigh();
        if (low.compareTo(high) > 0) {
          return null;
        } else {
          return new Range(low, high);
        }
      }
    }, param, rvalues);
    return set;
  }

  @Override
  protected NumberSet visitLess(Less obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    NumberSet set = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow();
        BigInteger high = right.getHigh().subtract(BigInteger.ONE);
        high = high.min(left.getHigh());
        if (low.compareTo(high) > 0) {
          return null;
        } else {
          return new Range(low, high);
        }
      }
    }, param, rvalues);
    return set;
  }

  @Override
  protected NumberSet visitLessequal(Lessequal obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    NumberSet set = NumberSet.execOp(new NumberSetOperator() {
      @Override
      public Range op(Range left, Range right) {
        BigInteger low = left.getLow();
        BigInteger high = right.getHigh();
        high = high.min(left.getHigh());
        if (low.compareTo(high) > 0) {
          return null;
        } else {
          return new Range(low, high);
        }
      }
    }, param, rvalues);
    return set;
  }

  @Override
  protected NumberSet visitEqual(Equal obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    NumberSet retset = NumberSet.intersection(rvalues, param);
    return retset;
  }

  @Override
  protected NumberSet visitNotequal(Notequal obj, NumberSet param) {
    NumberSet rvalues = getRange(obj);
    if (rvalues.getNumberCount().equals(BigInteger.ONE)) {
      NumberSet isec = NumberSet.intersection(param, rvalues);
      NumberSet ret = NumberSet.invert(isec, param);
      return ret;
    } else {
      return param;
    }
  }

}
