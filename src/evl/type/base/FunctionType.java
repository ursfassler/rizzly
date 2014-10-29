package evl.type.base;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.other.EvlList;
import evl.type.Type;

final public class FunctionType extends Type {
  private EvlList<SimpleRef<Type>> arg;
  final private SimpleRef<Type> ret;

  public FunctionType(ElementInfo info, String name, EvlList<SimpleRef<Type>> arg, SimpleRef<Type> ret) {
    super(info, name);
    this.arg = arg;
    this.ret = ret;
  }

  public EvlList<SimpleRef<Type>> getArg() {
    return arg;
  }

  public SimpleRef<Type> getRet() {
    return ret;
  }

  @Override
  public String toString() {
    return "func(" + arg + "):" + ret;
  }

}
