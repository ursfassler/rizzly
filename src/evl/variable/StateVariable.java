package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.SimpleRef;
import evl.hfsm.StateItem;
import evl.type.Type;

final public class StateVariable extends DefVariable implements StateItem {

  public StateVariable(ElementInfo info, String name, SimpleRef<Type> type, Expression def) {
    super(info, name, type, def);
  }

}
