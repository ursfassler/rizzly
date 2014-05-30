package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;

final public class ConstPrivate extends Constant {
  public ConstPrivate(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
