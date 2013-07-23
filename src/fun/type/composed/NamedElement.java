package fun.type.composed;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;
import fun.other.Named;

public class NamedElement extends FunBase implements Named {
  private String name;
  private Expression type;

  public NamedElement(ElementInfo info, String name, Expression type) {
    super(info);
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Expression getType() {
    return type;
  }

  public void setType(Expression type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name + ":" + type.toString();
  }

}
