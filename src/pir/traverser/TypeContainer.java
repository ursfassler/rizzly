package pir.traverser;

import pir.NullTraverser;
import pir.PirObject;
import pir.type.BooleanType;
import pir.type.RangeType;
import pir.type.Type;
import pir.type.UnsignedType;

public class TypeContainer extends NullTraverser<Boolean, Type> {

  static public boolean leftIsContainer(Type left, Type right) {
    TypeContainer container = new TypeContainer();
    return container.traverse(left, right);
  }

  @Override
  protected Boolean doDefault(PirObject obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitUnsignedType(UnsignedType obj, Type param) {
    if (param instanceof UnsignedType) {
      return obj.getBits() >= ((UnsignedType) param).getBits();
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitRangeType(RangeType obj, Type param) {
    if (param instanceof RangeType) {
      int cmpLow = obj.getLow().compareTo(((RangeType) param).getLow());
      int cmpHigh = obj.getHigh().compareTo(((RangeType) param).getHigh());
      return (cmpLow <= 0) && (cmpHigh >= 0); // TODO ok?
    } else {
      return false; // TODO correct?
    }
  }

  @Override
  protected Boolean visitBooleanType(BooleanType obj, Type param) {
    return param instanceof BooleanType;
  }

}
