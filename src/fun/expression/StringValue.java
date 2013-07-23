package fun.expression;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
final public class StringValue extends Expression {

  final private String value;

  public StringValue(ElementInfo info, String value) {
    super(info);
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "'" + value + "'";
  }

}
