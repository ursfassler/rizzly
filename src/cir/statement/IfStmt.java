package cir.statement;

import cir.expression.Expression;

public class IfStmt extends Statement {
  private Expression condition;
  private Statement thenBlock;
  private Statement elseBlock;

  public IfStmt(Expression condition, Statement thenBlock, Statement elseBlock) {
    super();
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  public Expression getCondition() {
    return condition;
  }

  public Statement getThenBlock() {
    return thenBlock;
  }

  public Statement getElseBlock() {
    return elseBlock;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public void setThenBlock(Statement thenBlock) {
    this.thenBlock = thenBlock;
  }

  public void setElseBlock(Statement elseBlock) {
    this.elseBlock = elseBlock;
  }

  @Override
  public String toString() {
    return "if( " + condition + " )";
  }

}
