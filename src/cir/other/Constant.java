package cir.other;

import cir.expression.Expression;
import cir.type.Type;

public class Constant extends Variable {
  private Expression def; // TODO change it to an constant initializer

  public Constant(String name, Type type, Expression def) {
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
