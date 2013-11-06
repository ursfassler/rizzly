package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;

/**
 * 
 * @author urs
 */
public class WhileStmt extends Statement {

  private Expression condition;
  private Block body;

  public WhileStmt(ElementInfo info, Expression condition, Block body) {
    super(info);
    this.condition = condition;
    this.body = body;
  }

  public Block getBody() {
    return body;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  @Override
  public String toString() {
    return "while " + condition;
  }
}
