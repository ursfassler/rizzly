package cir.expression;

public class ElementValue extends Expression {
  private String name;
  private Expression value;

  public ElementValue(String name, Expression value) {
    super();
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

}
