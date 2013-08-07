package evl.function;

import evl.cfg.BasicBlockList;

public interface FuncWithBody extends FunctionHeader{
  public BasicBlockList getBody();

  public void setBody(BasicBlockList body);
}
