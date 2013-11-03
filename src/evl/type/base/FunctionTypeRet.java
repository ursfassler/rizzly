package evl.type.base;

import java.util.List;

import common.ElementInfo;

import evl.type.TypeRef;

final public class FunctionTypeRet extends FunctionType {
  final private TypeRef ret;

  public FunctionTypeRet(ElementInfo info, String name, List<TypeRef> arg, TypeRef ret) {
    super(info, name, arg);
    this.ret = ret;
  }

  public TypeRef getRet() {
    return ret;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + ret;
  }

}
