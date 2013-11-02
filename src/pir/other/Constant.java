package pir.other;

import pir.expression.Expression;
import pir.type.TypeRef;

final public class Constant extends Variable {
  private Expression def;

  public Constant(String name, TypeRef type, Expression def) {
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
