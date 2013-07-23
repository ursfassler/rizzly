package pir.function.impl;

import java.util.List;

import pir.function.FuncWithRet;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.type.Type;

final public class FuncProtoRet extends Function implements FuncWithRet {
  private Type retType = null;

  public FuncProtoRet(String name, List<FuncVariable> argument) {
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
}
