package fun.type.template;

import java.math.BigInteger;

import common.ElementInfo;

import fun.expression.reference.SimpleRef;
import fun.type.Type;
import fun.type.base.BaseType;

public class Array extends BaseType {
  private SimpleRef type;
  private BigInteger size;

  public Array(ElementInfo info, BigInteger size, SimpleRef type) {
    super(info, makeName(size, type));
    this.type = type;
    this.size = size;
  }

  public static String makeName(BigInteger size, SimpleRef type) {
    return ArrayTemplate.NAME + "{" + size + "," + ((Type) type.getLink()).getName() + "}";
  }

  public SimpleRef getType() {
    return type;
  }

  public void setType(SimpleRef type) {
    this.type = type;
  }

  public void setSize(BigInteger size) {
    this.size = size;
  }

  public BigInteger getSize() {
    return size;
  }
}
