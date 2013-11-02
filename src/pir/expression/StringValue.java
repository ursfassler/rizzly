package pir.expression;

public class StringValue extends Expression {
  final private String value;

  public StringValue(String value) {
    super();
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }

}
