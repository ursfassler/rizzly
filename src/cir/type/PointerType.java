package cir.type;

public class PointerType extends Type {
  private TypeRef type;

  public PointerType(String name, TypeRef type) {
    super(name);
    this.type = type;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

}
