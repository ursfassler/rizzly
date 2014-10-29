package evl.type.base;

import java.math.BigInteger;

import common.ElementInfo;

import evl.Evl;
import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class ArrayType extends BaseType implements Evl {
  private SimpleRef<Type> type;
  private BigInteger size;

  public ArrayType(BigInteger size, SimpleRef<Type> type) {
    super(ElementInfo.NO, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public ArrayType(ElementInfo info, String name, BigInteger size, SimpleRef<Type> type) {
    super(info, name);
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, SimpleRef<Type> type) {
    return "Array{" + size + "," + type.getLink().getName() + "}";
  }

  public String makeName() {
    return makeName(size, type);
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }

  public BigInteger getSize() {
    return size;
  }

}
