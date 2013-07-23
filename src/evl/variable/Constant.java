package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.Type;

abstract public class Constant extends Variable {
  private Expression def;

  public Constant(ElementInfo info, String name, Type type, Expression def) {
    super(info, name, type);
    this.def = def;
  }

  public Expression getDef() {
    return def;
  }

  public void setDef(Expression def) {
    assert (def != null);
    this.def = def;
  }

}
