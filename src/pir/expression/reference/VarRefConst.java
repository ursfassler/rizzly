package pir.expression.reference;

import pir.expression.PExpression;
import pir.other.Constant;
import pir.other.PirValue;
import pir.type.TypeRef;

/**
 *
 * @author urs
 */
public class VarRefConst extends PExpression implements Reference<Constant>, PirValue {
  private Constant ref;

  public VarRefConst(Constant ref) {
    super();
    this.ref = ref;
  }

  @Override
  public Constant getRef() {
    return ref;
  }

  @Override
  public void setRef(Constant ref) {
    this.ref = ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

  @Override
  public TypeRef getType() {
    return ref.getType();
  }

}
