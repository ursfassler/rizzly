package evl.expression;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.type.Type;

public class UnsafeUnionValue extends Expression {
  private NamedElementValue contentValue;
  private SimpleRef<Type> type;

  public UnsafeUnionValue(ElementInfo info, NamedElementValue contentValue, SimpleRef<Type> type) {
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

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }
}
