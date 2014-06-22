package fun.function.impl;

import common.ElementInfo;

import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.statement.Block;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 * 
 * @author urs
 */
public class FuncImplSlot extends FunctionHeader implements FuncWithBody {
  private Block body;

  public FuncImplSlot(ElementInfo info) {
    super(info);
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
