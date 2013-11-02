package pir.function.impl;

import java.util.List;

import pir.function.FuncWithRet;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.type.TypeRef;

final public class FuncProtoRet extends Function implements FuncWithRet {
  private TypeRef retType = null;

  public FuncProtoRet(String name, List<FuncVariable> argument) {
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
}
