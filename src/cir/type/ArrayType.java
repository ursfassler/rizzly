package cir.type;

public class ArrayType extends Type {
  private TypeRef type;
  private int size;

  public ArrayType(String name, TypeRef type, int size) {
    super(name);
    this.type = type;
    this.size = size;
  }

  public TypeRef getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public void setSize(int size) {
    this.size = size;
  }

}
