package pir.statement;

import pir.expression.reference.Reference;

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
    return "call";
  }
}
