package fun.expression;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
final public class Number extends Expression {

  final private int value;

  public Number(ElementInfo info, int value) {
    super(info);
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

}
