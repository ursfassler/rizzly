package pir.function;

import pir.type.TypeRef;

public interface FuncWithRet {
  public TypeRef getRetType();

  public void setRetType(TypeRef retType);

}
