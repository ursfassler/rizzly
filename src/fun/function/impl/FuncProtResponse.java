package fun.function.impl;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;

/**
 * 
 * @author urs
 */
public class FuncProtResponse extends FunctionHeader implements FuncWithReturn {
  private Reference ret;

  public FuncProtResponse(ElementInfo info) {
    super(info);
  }

  @Override
  public Reference getRet() {
    return ret;
  }

  @Override
  public void setRet(Reference ret) {
    this.ret = ret;
  }

}
