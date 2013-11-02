package cir.type;

abstract public class IntType extends Type {
  private final int bytes;

  public IntType(String name, int bytes) {
    super(name);
    this.bytes = bytes;
  }

  public int getBytes() {
    return bytes;
  }

}
