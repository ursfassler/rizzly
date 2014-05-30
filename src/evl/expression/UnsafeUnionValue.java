package evl.expression;

import common.ElementInfo;

import evl.type.TypeRef;

public class UnsafeUnionValue extends Expression {
  private NamedElementValue contentValue;
  private TypeRef type;

  public UnsafeUnionValue(ElementInfo info, NamedElementValue contentValue, TypeRef type) {
    super(info);
    this.contentValue = contentValue;
    this.type = type;
  }

  public NamedElementValue getContentValue() {
    return contentValue;
  }

  public void setContentValue(NamedElementValue contentValue) {
    this.contentValue = contentValue;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }
}
