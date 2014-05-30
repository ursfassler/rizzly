package cir.expression;

public class UnsafeUnionValue extends Expression {
  private ElementValue contentValue;

  public UnsafeUnionValue(ElementValue contentValue) {
    super();
    this.contentValue = contentValue;
  }

  public ElementValue getContentValue() {
    return contentValue;
  }

  public void setContentValue(ElementValue contentValue) {
    this.contentValue = contentValue;
  }

}
