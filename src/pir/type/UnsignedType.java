package pir.type;

public class UnsignedType extends Type implements IntType {
  private final int bits;

  public UnsignedType(int bits) {
    super("U" + "{" + bits + "}");
    this.bits = bits;
  }

  public int getBits() {
    return bits;
  }

}
