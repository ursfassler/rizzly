package evl.statement;

import common.ElementInfo;

import evl.expression.reference.Reference;

public class CallStmt extends Statement {
  private Reference call;

  public CallStmt(ElementInfo info, Reference call) {
    super(info);
    this.call = call;
  }

  public Reference getCall() {
    return call;
  }

  public void setCall(Reference call) {
    this.call = call;
  }

  @Override
  public String toString() {
    return call.toString();
  }
}
