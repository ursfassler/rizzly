package evl.function;

import evl.type.TypeRef;

public interface FuncWithReturn extends FunctionHeader {
  public TypeRef getRet();

  public void setRet(TypeRef ret);
}
