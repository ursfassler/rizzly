package pir.type;


final public class TypeAlias extends Type {
  private TypeRef ref;

  public TypeAlias(String name, TypeRef ref) {
    super(name);
    this.ref = ref;
  }

  public TypeRef getRef() {
    return ref;
  }

  public void setRef(TypeRef ref) {
    this.ref = ref;
  }

}
