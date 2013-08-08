package pir.function.impl;

import java.util.List;

import pir.cfg.BasicBlockList;
import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.FuncVariable;

final public class FuncImplVoid extends Function implements FuncWithBody {
  private BasicBlockList body = null;

  public FuncImplVoid(String name, List<FuncVariable> argument) {
    super(name, argument);
  }

  @Override
  public BasicBlockList getBody() {
    assert (body != null);
    return body;
  }

  @Override
  public void setBody(BasicBlockList body) {
    assert (body != null);
    this.body = body;
  }

}
