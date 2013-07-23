package cir.expression;

import cir.expression.reference.RefItem;

public class Reference extends Expression {
  final private RefItem ref;

  public Reference(RefItem ref) {
    super();
    this.ref = ref;
  }

  public RefItem getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
