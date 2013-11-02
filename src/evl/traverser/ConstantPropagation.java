package evl.traverser;

import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.other.RizzlyProgram;
import evl.type.Type;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.variable.Constant;

/**
 * Propagates (some) constant values where they are used
 * 
 */
public class ConstantPropagation extends ExprReplacer<Void> {
  final private static ConstantPropagation INSTANCE = new ConstantPropagation();

  public static void process(RizzlyProgram prg) {
    INSTANCE.traverse(prg, null);
  }

  private boolean doReduce(Type type) {
    if (type instanceof RangeType) {
      return true;
    } else if (type instanceof EnumType) {
      return true;
    }
    throw new RuntimeException("not yet implemented:" + type.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitReference(Reference obj, Void param) {
    if (obj.getLink() instanceof Constant) {
      Constant constant = (Constant) obj.getLink();
      Type type = constant.getType().getRef();
      if (doReduce(type)) {
        assert (obj.getOffset().isEmpty());
        return visit(constant.getDef(), null);
      }
    }
    return super.visitReference(obj, param);
  }

}
