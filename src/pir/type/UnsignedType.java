package pir.type;

public class UnsignedType extends Type implements IntType {
  private final int bits;

  public UnsignedType(int bits) {
    super(makeName(bits));
    this.bits = bits;
  }

  public static String makeName(int bits) {
    return "U" + "{" + bits + "}";
  }

  public int getBits() {
    return bits;
  }

}
