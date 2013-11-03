package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncIfaceIn;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.type.TypeRef;
import evl.variable.FuncVariable;

public class FuncIfaceInRet extends FunctionBase implements FuncIfaceIn, FuncWithReturn {
  private TypeRef ret = null;

  public FuncIfaceInRet(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

  @Override
  public TypeRef getRet() {
    assert (ret != null);
    return ret;
  }

  @Override
  public void setRet(TypeRef ret) {
    assert (ret != null);
    this.ret = ret;
  }

}
