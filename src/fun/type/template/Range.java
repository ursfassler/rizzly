package fun.type.template;

import java.math.BigInteger;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.other.Named;
import fun.type.base.BaseType;
import fun.variable.TemplateParameter;

final public class Range extends BaseType implements Named {
  final private BigInteger low;
  final private BigInteger high;

  public Range(ElementInfo info, BigInteger low, BigInteger high) {
    super(info, makeName(low, high));
    assert (low.compareTo(high) <= 0); // TODO ok?
    this.low = low;
    this.high = high;
  }

  public static String makeName(BigInteger low, BigInteger high) {
    return RangeTemplate.NAME + "{" + low.toString() + "," + high.toString() + "}";
  }

  public BigInteger getLow() {
    return low;
  }

  public BigInteger getHigh() {
    return high;
  }

  @Override
  public ListOfNamed<TemplateParameter> getParamList() {
    return new ListOfNamed<TemplateParameter>();
  }
}
