package evl.type.base;

import java.util.List;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.type.Type;


final public class FunctionTypeRet extends FunctionType {
  final private Reference ret;

  public FunctionTypeRet(ElementInfo info, String name, List<Type> arg, Reference ret) {
    super(info, name, arg);
    this.ret = ret;
  }

  public Reference getRet() {
    return ret;
  }

  @Override
  public String toString() {
    return super.toString() + ":" + ret;
  }

}
