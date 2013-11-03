package pir.type;

import java.math.BigInteger;

public class ArrayType extends Type {
  private TypeRef type;
  private BigInteger size;

  public ArrayType(BigInteger size, TypeRef type) {
    super(makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, TypeRef type) {
    return "Array{" + size.toString() + "," + type.getRef().getName() + "}";
  }

  public TypeRef getType() {
    return type;
  }

  public BigInteger getSize() {
    return size;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public void setSize(BigInteger size) {
    this.size = size;
  }

}
