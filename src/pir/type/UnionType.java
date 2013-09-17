package pir.type;

public class UnionType extends NamedElemType {

  private UnionSelector selector;

  public UnionType(String name, UnionSelector selector) {
    super(name);
    this.selector = selector;
  }

  public UnionSelector getSelector() {
    return selector;
  }

  public void setSelector(UnionSelector selector) {
    this.selector = selector;
  }
}
