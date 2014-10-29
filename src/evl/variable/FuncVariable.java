package evl.variable;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.type.Type;

final public class FuncVariable extends Variable {

  public FuncVariable(ElementInfo info, String name, SimpleRef<Type> type) {
    super(info, name, type);
  }

}
