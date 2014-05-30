package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;

final public class StateVariable extends DefVariable {

  public StateVariable(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
