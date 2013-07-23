package pir.statement;

import pir.expression.PExpression;

public class WhileStmt extends Statement {
  private PExpression cond;
  private Block block;

  public WhileStmt(PExpression cond, Block block) {
    super();
    this.cond = cond;
    this.block = block;
  }

  public PExpression getCondition() {
    return cond;
  }

  public Block getBlock() {
    return block;
  }

  public PExpression getCond() {
    return cond;
  }

  public void setCond(PExpression cond) {
    this.cond = cond;
  }

  public void setBlock(Block block) {
    this.block = block;
  }

}
