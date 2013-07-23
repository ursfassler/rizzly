package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;

abstract public class Constant extends Variable {
  private Expression def;

  public Constant(ElementInfo info, String name, Expression type) {
    super(info, name, type);
  }

  public Expression getDef() {
    return def;
  }

  public void setDef(Expression def) {
    assert (def != null);
    this.def = def;
  }

}
