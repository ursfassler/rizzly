package fun.variable;

import common.ElementInfo;

import fun.expression.Expression;

final public class ConstGlobal extends Constant {
  public ConstGlobal(ElementInfo info, String name, Expression type) {
    super(info, name, type);
  }

}
