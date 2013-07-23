package evl.type.base;

import java.util.List;

import common.ElementInfo;

import evl.type.Type;

abstract public class FunctionType extends Type {
  private List<Type> arg;

  public FunctionType(ElementInfo info, String name, List<Type> arg) {
    super(info, name);
    this.arg = arg;
  }

  public List<Type> getArgD() {
    return arg;
  }

  @Override
  public String toString() {
    return "Function:" + getName() + arg;
  }

}
