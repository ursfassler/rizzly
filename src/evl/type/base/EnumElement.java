package evl.type.base;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.TypeRef;
import evl.variable.Constant;

final public class EnumElement extends Constant {

  public EnumElement(ElementInfo info, String name, TypeRef type, Expression def) {
    super(info, name, type, def);
  }
}
