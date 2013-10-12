package fun.variable;

import common.ElementInfo;

import fun.expression.reference.Reference;

final public class ConstGlobal extends Constant {
  public ConstGlobal(ElementInfo info, String name, Reference type) {
    super(info, name, type);
  }

}
