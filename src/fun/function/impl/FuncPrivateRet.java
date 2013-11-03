package fun.function.impl;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FuncWithBody;
import fun.function.FuncWithReturn;
import fun.function.FunctionHeader;
import fun.statement.Block;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 * 
 * @author urs
 */
public class FuncPrivateRet extends FunctionHeader implements FuncWithBody, FuncWithReturn {
  private Reference ret;
  private Block body;

  public FuncPrivateRet(ElementInfo info) {
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
