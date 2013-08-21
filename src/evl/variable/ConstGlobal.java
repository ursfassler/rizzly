package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.type.TypeRef;

final public class ConstGlobal extends Constant {
  public ConstGlobal(ElementInfo info, String name, TypeRef type, Expression def) {
    super(info, name, type, def);
  }

}
