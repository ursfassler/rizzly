package evl.type.base;

import java.util.List;

import common.ElementInfo;

import evl.type.Type;
import evl.type.TypeRef;

abstract public class FunctionType extends Type {
  private List<TypeRef> arg;

  public FunctionType(ElementInfo info, String name, List<TypeRef> arg) {
    super(info, name);
    this.arg = arg;
  }

  public List<TypeRef> getArgD() {
    return arg;
  }

  @Override
  public String toString() {
    return "Function:" + getName() + arg;
  }

}
