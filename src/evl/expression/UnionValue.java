package evl.expression;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class UnionValue extends Expression {
  private NamedElementValue tagValue;
  private NamedElementValue contentValue;
  private SimpleRef<Type> type;

  public UnionValue(ElementInfo info, NamedElementValue tagValue, NamedElementValue contentValue, SimpleRef<Type> type) {
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

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }
}
