package evl.type.base;

import java.util.List;

import common.ElementInfo;

import evl.type.Type;


final public class FunctionTypeVoid extends FunctionType {
  public FunctionTypeVoid(ElementInfo info, String name, List<Type> arg) {
    super(info, name, arg);
  }

  @Override
  public String toString() {
    return super.toString();
  }

}
