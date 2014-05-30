package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;

abstract public class Constant extends DefVariable {

  public Constant(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
