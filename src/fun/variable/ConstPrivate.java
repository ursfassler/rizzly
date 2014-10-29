package fun.variable;

import common.ElementInfo;

import fun.content.ElementaryContent;
import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.hfsm.StateContent;

final public class ConstPrivate extends Constant implements ElementaryContent, StateContent {
  public ConstPrivate(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
