package pir.other;

import pir.expression.PExpression;
import pir.type.TypeRef;

final public class Constant extends Variable {
  private PExpression def;

  public Constant(String name, TypeRef type, PExpression def) {
    super(name, type);
    this.def = def;
  }

  public PExpression getDef() {
    return def;
  }

  public void setDef(PExpression def) {
    this.def = def;
  }

}
