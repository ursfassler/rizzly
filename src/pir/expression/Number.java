package pir.expression;

import java.math.BigInteger;

import pir.other.PirValue;
import pir.type.TypeRef;

public class Number extends PExpression implements PirValue {
  final private BigInteger value;
  private TypeRef type;

  public Number(BigInteger value, TypeRef type) {
    super();
    assert( value != null );
    assert( type != null );
    this.value = value;
    this.type = type;
  }

  public BigInteger getValue() {
    return value;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  @Override
  public TypeRef getType() {
    return type;
  }

  @Override
  public String toString() {
    return value.toString() + ":" + type.toString();
  }

}
