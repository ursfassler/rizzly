package fun.variable;

import common.ElementInfo;

import fun.content.ElementaryContent;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.hfsm.StateContent;

final public class StateVariable extends DefVariable implements ElementaryContent, StateContent {

  public StateVariable(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
