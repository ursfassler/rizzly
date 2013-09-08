package evl.type.special;

import common.ElementInfo;

import evl.Evl;
import evl.type.TypeRef;
import evl.type.base.BaseType;

public class PointerType extends BaseType implements Evl {

  private TypeRef type;

  public PointerType(TypeRef type) {
    super(new ElementInfo(), makeName(type));
    this.type = type;
  }

  public static String makeName(TypeRef type) {
    return "Pointer{" + type.getRef().getName() + "}";
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }
}
