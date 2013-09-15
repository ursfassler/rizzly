package pir.expression;

import pir.other.PirValue;
import pir.type.TypeRef;

public class BoolValue extends PExpression implements PirValue {
  final private boolean value;
  final private TypeRef type;

  public BoolValue(boolean value,TypeRef type) {
    super();
    this.value = value;
    this.type = type;
  }

  public boolean isValue() {
    return value;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  @Override
  public TypeRef getType() {
    return type;
  }

}
