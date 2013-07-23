package evl.hfsm;

public class QueryItem extends StateItem {
  private String namespace;
  private HfsmQueryFunction func;

  public QueryItem(String namespace, HfsmQueryFunction func) {
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

  public HfsmQueryFunction getFunc() {
    return func;
  }

  public void setFunc(HfsmQueryFunction func) {
    this.func = func;
  }

  @Override
  public String toString() {
    return namespace + "." + func;
  }

}
