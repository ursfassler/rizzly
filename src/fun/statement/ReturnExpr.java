package fun.statement;

import common.ElementInfo;

import fun.expression.Expression;

/**
 * 
 * @author urs
 */
public class ReturnExpr extends Return {

  private Expression expr;

  public ReturnExpr(ElementInfo info, Expression expr) {
    super(info);
    this.expr = expr;
  }

  public Expression getExpr() {
    return expr;
  }

  public void setExpr(Expression expr) {
    this.expr = expr;
  }

  @Override
  public String toString() {
    return super.toString() + " " + expr;
  }
}
