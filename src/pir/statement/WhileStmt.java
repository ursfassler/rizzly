package pir.statement;

import pir.expression.Expression;

public class WhileStmt extends Statement {
  private Expression cond;
  private Block block;

  public WhileStmt(Expression cond, Block block) {
    super();
    this.cond = cond;
    this.block = block;
  }

  public Expression getCondition() {
    return cond;
  }

  public Block getBlock() {
    return block;
  }

  public Expression getCond() {
    return cond;
  }

  public void setCond(Expression cond) {
    this.cond = cond;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

}
