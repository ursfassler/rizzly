package cir.statement;

import cir.expression.reference.Reference;

public class CallStmt extends Statement {
  final private Reference ref;

  public CallStmt(Reference ref) {
    super();
    this.ref = ref;
  }

  public Reference getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return ref.toString();
  }

}
