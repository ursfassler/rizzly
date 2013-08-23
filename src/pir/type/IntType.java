package pir.type;

abstract public class IntType extends Type {
  private final int bits;

  public IntType(String name, int bits) {
    super(name);
    this.bits = bits;
  }

  public int getBits() {
    return bits;
  }
}
