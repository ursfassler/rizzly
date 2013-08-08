package pir.cfg;

import java.math.BigInteger;

public class CaseOptRange extends CaseOptEntry {
  private BigInteger start;
  private BigInteger end;

  public CaseOptRange(BigInteger start, BigInteger end) {
    this.start = start;
    this.end = end;
  }

  public BigInteger getStart() {
    return start;
  }

  public void setStart(BigInteger start) {
    this.start = start;
  }

  public BigInteger getEnd() {
    return end;
  }

  public void setEnd(BigInteger end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return start.toString() + ".." + end.toString();
  }

}
