package fun.function.impl;

import common.ElementInfo;

import fun.expression.Expression;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionBodyImplementation;
import fun.function.FunctionHeader;

/**
 * Globally defined function. It is a pure function and can be executed at compile time.
 *
 * @author urs
 */
public class FuncGlobal extends FunctionHeader implements FuncWithBody, FuncWithReturn {
  private Expression ret;
  private FunctionBodyImplementation body;

  public FuncGlobal(ElementInfo info) {
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

  @Override
  public FunctionBodyImplementation getBody() {
    return body;
  }

  @Override
  public void setBody(FunctionBodyImplementation body) {
    this.body = body;
  }

}
