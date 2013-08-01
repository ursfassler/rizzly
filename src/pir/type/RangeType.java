package pir.type;

import java.math.BigInteger;

public class RangeType extends Type {
  private final BigInteger low;
  private final BigInteger high;

  public RangeType(BigInteger low, BigInteger high) {
    super("R" + "{" + low.toString() + "," + high.toString() + "}");
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

}
