package cir.type;

public class ArrayType extends Type {
  private Type type;
  private int size;

  public ArrayType(String name, Type type, int size) {
    super(name);
    this.type = type;
    this.size = size;
  }

  public Type getType() {
    return type;
  }

  public int getSize() {
    return size;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public void setSize(int size) {
    this.size = size;
  }

}
