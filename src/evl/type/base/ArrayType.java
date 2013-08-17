package evl.type.base;

import common.ElementInfo;

import evl.Evl;
import evl.expression.reference.Reference;

public class ArrayType extends BaseType implements Evl {
  private Reference type;
  private int size;

  public ArrayType(int size, Reference type) {
    super(new ElementInfo(), makeName(size, type.getLink().getName()));
    this.type = type;
    this.size = size;
  }

  public static String makeName(int size, String type) {
    return "Array{" + size + "," + type + "}";
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

  public int getSize() {
    return size;
  }

}
