package pir.statement;

import pir.expression.reference.CallExpr;

public class CallStmt extends Statement {
  final private CallExpr ref;

  public CallStmt(CallExpr ref) {
    super();
    this.ref = ref;
  }

  public CallExpr getCall() {
    return ref;
  }

  @Override
  public String toString() {
    return "call";
  }
}
