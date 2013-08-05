package fun.function.impl;

import common.ElementInfo;

import fun.expression.Expression;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionBodyImplementation;
import fun.function.FunctionHeader;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 *
 * @author urs
 */
public class FuncPrivateRet extends FunctionHeader implements FuncWithBody, FuncWithReturn {
  private Expression ret;
  private FunctionBodyImplementation body;

  public FuncPrivateRet(ElementInfo info) {
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
