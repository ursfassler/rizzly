package evl.expression;

import common.ElementInfo;

public class NamedElementValue extends Expression {
  private String name;
  private Expression value;

  public NamedElementValue(ElementInfo info, String name, Expression value) {
    super(info);
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  public Expression getValue() {
    return value;
  }

  @Override
  public String toString() {
    return name + ":=" + value;
  }

}
