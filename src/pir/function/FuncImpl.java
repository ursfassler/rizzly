package pir.function;

import java.util.List;

import pir.cfg.BasicBlockList;
import pir.other.SsaVariable;
import pir.type.Type;

final public class FuncImpl extends Function implements FuncWithBody {
  private BasicBlockList body = null;

  public FuncImpl(String name, List<SsaVariable> argument, Type retType) {
    super(name, argument, retType);
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
