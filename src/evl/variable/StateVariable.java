package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.TypeRef;

final public class StateVariable extends DefVariable {

  public StateVariable(ElementInfo info, String name, TypeRef type, Expression def) {
    super(info, name, type, def);
  }

}
