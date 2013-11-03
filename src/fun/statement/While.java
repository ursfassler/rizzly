package fun.statement;

import common.ElementInfo;

import fun.expression.Expression;

/**
 * 
 * @author urs
 */
public class While extends Statement {

  private Expression condition;
  private Block body;

  public While(ElementInfo info, Expression condition, Block body) {
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
