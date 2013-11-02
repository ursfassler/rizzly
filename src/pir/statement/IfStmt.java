package pir.statement;

import pir.expression.Expression;

public class IfStmt extends Statement {
  private Expression condition;
  private Block thenBlock;
  private Block elseBlock;

  public IfStmt(Expression condition, Block thenBlock, Block elseBlock) {
    super();
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public Block getThenBlock() {
    return thenBlock;
  }

  public void setThenBlock(Block thenBlock) {
    this.thenBlock = thenBlock;
  }

  public Block getElseBlock() {
    return elseBlock;
  }

  public void setElseBlock(Block elseBlock) {
    this.elseBlock = elseBlock;
  }

  @Override
  public String toString() {
    return "if " + condition;
  }
}
