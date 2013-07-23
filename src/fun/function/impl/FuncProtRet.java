package fun.function.impl;

import common.ElementInfo;

import fun.expression.Expression;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;

/**
 *
 * @author urs
 */
public class FuncProtRet extends FunctionHeader implements FuncWithReturn {
  private Expression ret;

  public FuncProtRet(ElementInfo info) {
    super(info);
  }

  @Override
  public Expression getRet() {
    return ret;
  }

  @Override
  public void setRet(Expression ret) {
    this.ret = ret;
  }

}
