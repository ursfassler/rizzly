package fun.variable;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;
import fun.other.Named;

abstract public class Variable extends FunBase implements Named {
  private String name;
  private Expression type;

  public Variable(ElementInfo info, String name, Expression type) {
    super(info);
    this.name = name;
    this.type = type;
  }

  public Expression getType() {
    return type;
  }

  public void setType(Expression type) {
    this.type = type;
  }

  public String getName() {
    assert (name != null);
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + ":" + type.toString();
  }
}
