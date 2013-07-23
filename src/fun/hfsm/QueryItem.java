package fun.hfsm;

import fun.function.impl.FuncPrivateRet;

public class QueryItem extends StateItem {
  private String namespace;
  private FuncPrivateRet func;

  public QueryItem(String namespace, FuncPrivateRet func) {
    super(func.getInfo());
    this.namespace = namespace;
    this.func = func;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public FuncPrivateRet getFunc() {
    return func;
  }

  public void setFunc(FuncPrivateRet func) {
    this.func = func;
  }

  @Override
  public String toString() {
    return namespace + "." + func;
  }

}
