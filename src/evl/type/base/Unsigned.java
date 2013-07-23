package evl.type.base;

import common.ElementInfo;


final public class Unsigned extends BaseType {
  final private int bits;

  public Unsigned(int bits) {
    super(new ElementInfo(), makeName(bits));
    this.bits = bits;
  }

  public static String makeName(int bits) {
    return "U{" + makeBitString(bits) + "}";
  }

  private static String makeBitString(int bits) {
    if (bits >= 0) {
      return Integer.toString(bits);
    } else {
      throw new RuntimeException("Wrong number of bits: " + bits);
    }
  }

  public int getBits() {
    return bits;
  }

}
