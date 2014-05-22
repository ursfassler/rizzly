package cir.type;

public class UnionType extends NamedElemType {
  private NamedElement tag;

  public UnionType(String name, NamedElement tag) {
    super(name);
    this.tag = tag;
  }

  public NamedElement getTag() {
    return tag;
  }

  public void setTag(NamedElement tag) {
    this.tag = tag;
  }

}
