package pir.cfg;

import pir.expression.PExpression;


/**
 *
 * @author urs
 */
public class ReturnExpr extends Return {

  private PExpression expr;

  public ReturnExpr(PExpression expr) {
    this.expr = expr;
  }

  public PExpression getExpr() {
    return expr;
  }

  public void setExpr(PExpression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return super.toString() + " " + expr;
  }
}
