package evl.function.impl;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

/**
 *
 * @author urs
 */
public class FuncProtoRet extends FunctionBase implements FuncWithReturn {
  private Reference ret = null;

  public FuncProtoRet(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

  @Override
  public Reference getRet() {
    assert (ret != null);
    return ret;
  }

  @Override
  public void setRet(Reference ret) {
    assert (ret != null);
    this.ret = ret;
  }

}
