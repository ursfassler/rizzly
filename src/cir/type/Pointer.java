package cir.type;

public class Pointer extends Type {
  private Type type;

  public Pointer(String name, Type type) {
    super(name);
    this.type = type;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

}
