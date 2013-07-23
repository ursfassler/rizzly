package evl.function;

import evl.expression.reference.Reference;

public interface FuncWithReturn extends FunctionHeader {
  public Reference getRet();

  public void setRet(Reference ret);
}
