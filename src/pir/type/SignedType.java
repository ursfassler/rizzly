package pir.type;

public class SignedType extends Type implements IntType {
  private final int bits;

  public SignedType(int bits) {
    super("S" + "{" + bits + "}");
    this.bits = bits;
  }

  public int getBits() {
    return bits;
  }

}
