package evl.statement.normal;

import evl.statement.normal.NormalStmt;
import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.Reference;

/**
 *
 * @author urs
 */
public class StoreStmt extends NormalStmt {

  private Reference address;
  private Expression expr;

  public StoreStmt(ElementInfo info, Reference address, Expression expr) {
    super(info);
    this.address = address;
    this.expr = expr;
  }

  public Reference getAddress() {
    return address;
  }

  public void setAddress(Reference address) {
    this.address = address;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return "store(" + address + " <= " + expr + ")";
  }
}
