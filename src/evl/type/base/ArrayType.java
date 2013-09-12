package evl.type.base;

import java.math.BigInteger;

import common.ElementInfo;

import evl.Evl;
import evl.type.TypeRef;

public class ArrayType extends BaseType implements Evl {

  private TypeRef type;
  private BigInteger size;

  public ArrayType(BigInteger size, TypeRef type) {
    super(new ElementInfo(), makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, TypeRef type) {
    return "Array{" + size + "," + type.getRef().getName() + "}";
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public BigInteger getSize() {
    return size;
  }
}
