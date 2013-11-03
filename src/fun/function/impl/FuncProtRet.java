package fun.function.impl;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;

/**
 * 
 * @author urs
 */
public class FuncProtRet extends FunctionHeader implements FuncWithReturn {
  private Reference ret;

  public FuncProtRet(ElementInfo info) {
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
