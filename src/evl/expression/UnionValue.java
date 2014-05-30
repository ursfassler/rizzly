package evl.expression;

import common.ElementInfo;

import evl.type.TypeRef;

public class UnionValue extends Expression {
  private NamedElementValue tagValue;
  private NamedElementValue contentValue;
  private TypeRef type;

  public UnionValue(ElementInfo info, NamedElementValue tagValue, NamedElementValue contentValue, TypeRef type) {
    super(info);
    this.tagValue = tagValue;
    this.contentValue = contentValue;
    this.type = type;
  }

  public NamedElementValue getTagValue() {
    return tagValue;
  }

  public void setTagValue(NamedElementValue tagValue) {
    this.tagValue = tagValue;
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
