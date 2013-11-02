package cir.statement;

import cir.expression.Expression;

public class WhileStmt extends Statement {
  private Expression cond;
  private Statement block;

  public WhileStmt(Expression cond, Statement block) {
    super();
    this.cond = cond;
    this.block = block;
  }

  public Expression getCondition() {
    return cond;
  }

  public Statement getBlock() {
    return block;
  }

  public Expression getCond() {
    return cond;
  }

  public void setCond(Expression cond) {
    this.cond = cond;
  }

  public void setBlock(Statement block) {
    this.block = block;
  }

  @Override
  public String toString() {
    return "while( " + cond + " )";
  }

}
