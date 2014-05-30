package cir.other;

import cir.expression.Expression;
import cir.type.TypeRef;

abstract public class DefVariable extends Variable {
  private Expression def;

  public DefVariable(String name, TypeRef type, Expression def) {
    super(name, type);
    this.def = def;
  }

  public Expression getDef() {
    return def;
  }

  public void setDef(Expression def) {
    this.def = def;
  }

}
