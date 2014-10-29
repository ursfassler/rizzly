package cir.variable;

import cir.expression.Expression;
import cir.type.TypeRef;

public class Constant extends DefVariable {

  public Constant(String name, TypeRef type, Expression def) {
    super(name, type, def);
  }

}
