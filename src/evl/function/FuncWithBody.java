package evl.function;

import evl.statement.Block;

public interface FuncWithBody extends FunctionHeader {
  public Block getBody();

  public void setBody(Block body);
}
