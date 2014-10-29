package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.SimpleRef;
import evl.type.Type;

abstract public class DefVariable extends Variable {
  private Expression def;

  public DefVariable(ElementInfo info, String name, SimpleRef<Type> type, Expression def) {
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

  @Override
  public String toString() {
    return super.toString() + "=" + def;
  }

}
