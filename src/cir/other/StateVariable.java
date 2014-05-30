package cir.other;

import cir.expression.Expression;
import cir.type.TypeRef;

public class StateVariable extends DefVariable {

  public StateVariable(String name, TypeRef type, Expression def) {
    super(name, type, def);
  }

}
