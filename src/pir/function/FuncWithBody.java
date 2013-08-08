package pir.function;

import pir.cfg.BasicBlockList;

public interface FuncWithBody {
  public BasicBlockList getBody();

  public void setBody(BasicBlockList body);

}
