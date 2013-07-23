package pir.expression.reference;

final public class RefName extends RefMiddle {
  final private String name;

  public RefName(RefItem previous, String name) {
    super(previous);
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
