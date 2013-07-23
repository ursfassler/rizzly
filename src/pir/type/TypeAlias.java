package pir.type;

final public class TypeAlias extends Type {
  private Type ref;

  public TypeAlias(String name, Type ref) {
    super(name);
    this.ref = ref;
  }

  public Type getRef() {
    return ref;
  }

  public void setRef(Type ref) {
    this.ref = ref;
  }

}
