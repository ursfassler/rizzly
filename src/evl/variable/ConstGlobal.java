package evl.variable;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.SimpleRef;
import evl.type.Type;

final public class ConstGlobal extends Constant {
  public ConstGlobal(ElementInfo info, String name, SimpleRef<Type> type, Expression def) {
    super(info, name, type, def);
  }

}
