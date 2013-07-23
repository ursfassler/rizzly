package pir.expression.reference;

final public class RefHead extends RefItem {
  private Referencable ref;

  public RefHead(Referencable ref) {
    super();
    this.ref = ref;
  }

  public Referencable getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
