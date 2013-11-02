package pir.function.impl;

import java.util.List;

import pir.function.FuncWithBody;
import pir.function.FuncWithRet;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.statement.Block;
import pir.type.TypeRef;

final public class FuncImplRet extends Function implements FuncWithRet, FuncWithBody {
  private TypeRef retType = null;
  private Block body = null;

  public FuncImplRet(String name, List<FuncVariable> argument) {
    super(name, argument);
  }

  @Override
  public TypeRef getRetType() {
    return retType;
  }

  @Override
  public void setRetType(TypeRef retType) {
    this.retType = retType;
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
