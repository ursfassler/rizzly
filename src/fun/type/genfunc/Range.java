package fun.type.genfunc;

import java.math.BigInteger;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.other.Named;
import fun.type.base.BaseType;
import fun.variable.CompfuncParameter;

final public class Range extends BaseType implements Named {
  final private BigInteger low;
  final private BigInteger high;
  private String name;

  public Range(ElementInfo info, BigInteger low, BigInteger high) {
    super(info);
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
    name = "U{" + low.toString() + "," + high.toString() + "}";
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

  @Override
  public ListOfNamed<CompfuncParameter> getParamList() {
    return new ListOfNamed<CompfuncParameter>();
  }
}
