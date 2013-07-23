package pir.function.impl;

import java.util.List;

import pir.function.FuncWithBody;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.statement.Block;

final public class FuncImplVoid extends Function implements FuncWithBody {
  private Block body = null;

  public FuncImplVoid(String name, List<FuncVariable> argument) {
    super(name, argument);
  }

  @Override
  public Block getBody() {
    assert (body != null);
    return body;
  }

  @Override
  public void setBody(Block body) {
    assert (body != null);
    this.body = body;
  }

}
