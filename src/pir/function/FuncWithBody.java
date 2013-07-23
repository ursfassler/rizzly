package pir.function;

import pir.statement.Block;

public interface FuncWithBody {
  public Block getBody();

  public void setBody(Block body);

}
