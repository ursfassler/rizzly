package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;

abstract public class Constant extends Variable {
  private Expression def;

  public Constant(ElementInfo info, String name, Reference type) {
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
