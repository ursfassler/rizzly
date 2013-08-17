package pir.expression.reference;

final public class RefName extends RefItem {
  final private String name;

  public RefName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "." + name;
  }

}
