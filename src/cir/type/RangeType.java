package cir.type;

import java.math.BigInteger;

public class RangeType extends Type {
  private final BigInteger low;
  private final BigInteger high;

  public RangeType(BigInteger low, BigInteger high) {
    super(makeName(low, high));
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public static String makeName(BigInteger low, BigInteger high) {
    return "R" + "{" + low.toString() + "," + high.toString() + "}";
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

  public static RangeType makeContainer(RangeType lt, RangeType rt) {
    BigInteger low = lt.getLow().min(rt.getLow());
    BigInteger high = lt.getHigh().max(rt.getHigh());
    return new RangeType(low, high);
  }

  /**
   * 
   * @param lt
   * @param rt
   * @return (lt.low < rt.low) or (lt.high > rt.high)
   */
  public static boolean isBigger(RangeType lt, RangeType rt) {
    boolean lowIn = lt.getLow().compareTo(rt.getLow()) < 0; // TODO ok?
    boolean highIn = lt.getHigh().compareTo(rt.getHigh()) > 0; // TODO ok?
    return lowIn || highIn;
  }

  public static boolean isEqual(RangeType lt, RangeType rt) {
    boolean lowIn = lt.getLow().compareTo(rt.getLow()) == 0;
    boolean highIn = lt.getHigh().compareTo(rt.getHigh()) == 0;
    return lowIn && highIn;
  }
}