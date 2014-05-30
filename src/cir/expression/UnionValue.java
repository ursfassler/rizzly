package cir.expression;

public class UnionValue extends Expression {
  private ElementValue tagValue;
  private ElementValue contentValue;

  public UnionValue(ElementValue tagValue, ElementValue contentValue) {
    super();
    this.tagValue = tagValue;
    this.contentValue = contentValue;
  }

  public ElementValue getTagValue() {
    return tagValue;
  }

  public void setTagValue(ElementValue tagValue) {
    this.tagValue = tagValue;
  }

  public ElementValue getContentValue() {
    return contentValue;
  }

  public void setContentValue(ElementValue contentValue) {
    this.contentValue = contentValue;
  }

}
