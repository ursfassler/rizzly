package fun.type.base;

import common.ElementInfo;

import fun.expression.Expression;
import fun.expression.reference.Reference;
import fun.variable.Constant;

final public class EnumElement extends Constant {

  public EnumElement(ElementInfo info, String name, Reference type, Expression def) {
    super(info, name, type, def);
  }

}
