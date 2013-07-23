package pir.function;

import pir.type.Type;

public interface FuncWithRet {
  public Type getRetType();

  public void setRetType(Type retType);

}
