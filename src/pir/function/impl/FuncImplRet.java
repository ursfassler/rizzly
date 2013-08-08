package pir.function.impl;

import java.util.List;

import pir.cfg.BasicBlockList;
import pir.function.FuncWithBody;
import pir.function.FuncWithRet;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.type.Type;

final public class FuncImplRet extends Function implements FuncWithRet, FuncWithBody {
  private Type retType = null;
  private BasicBlockList body = null;

  public FuncImplRet(String name, List<FuncVariable> argument) {
    super(name, argument);
  }

  @Override
  public Type getRetType() {
    return retType;
  }

  @Override
  public void setRetType(Type retType) {
    this.retType = retType;
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
