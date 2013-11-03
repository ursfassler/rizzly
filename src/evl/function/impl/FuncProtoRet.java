package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.type.TypeRef;
import evl.variable.FuncVariable;

/**
 * 
 * @author urs
 */
public class FuncProtoRet extends FunctionBase implements FuncWithReturn {
  private TypeRef ret = null;

  public FuncProtoRet(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
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
