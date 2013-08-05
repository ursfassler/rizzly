package fun.function.impl;

import common.ElementInfo;

import fun.function.FuncWithBody;
import fun.function.FunctionBodyImplementation;
import fun.function.FunctionHeader;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 *
 * @author urs
 */
public class FuncEntryExit extends FunctionHeader implements FuncWithBody {
  private FunctionBodyImplementation body;

  public FuncEntryExit(ElementInfo info) {
    super(info);
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
