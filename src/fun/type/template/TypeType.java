package fun.type.template;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.Type;

public class TypeType extends Type {
  private Reference type;

  public TypeType(ElementInfo info, Reference type) {
    super(info);
    this.type = type;
  }

  public Reference getType() {
    return type;
  }

  public void setType(Reference type) {
    this.type = type;
  }

}
