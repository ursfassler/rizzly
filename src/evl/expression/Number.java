package evl.expression;

import java.math.BigInteger;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
final public class Number extends Expression {

  final private BigInteger value;

  public Number(ElementInfo info, BigInteger value) {
    super(info);
    this.value = value;
  }

  public BigInteger getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
