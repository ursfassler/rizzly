package fun.function.impl;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.statement.Block;

/**
 * Globally defined function. It is a pure function and can be executed at compile time.
 *
 * @author urs
 */
public class FuncGlobal extends FunctionHeader implements FuncWithBody, FuncWithReturn {
  private Reference ret;
  private Block body;

  public FuncGlobal(ElementInfo info) {
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

  @Override
  public Block getBody() {
    return body;
  }

  @Override
  public void setBody(Block body) {
    this.body = body;
  }

}
