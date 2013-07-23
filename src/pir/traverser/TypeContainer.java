package pir.traverser;

import pir.NullTraverser;
import pir.PirObject;
import pir.type.BooleanType;
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
  protected Boolean visitBooleanType(BooleanType obj, Type param) {
    return param instanceof BooleanType;
  }

}
