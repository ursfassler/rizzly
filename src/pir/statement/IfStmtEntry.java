package pir.statement;

import pir.PirObject;
import pir.expression.PExpression;

public class IfStmtEntry extends PirObject {
  private PExpression condition;
  private Block code;

  public IfStmtEntry(PExpression condition, Block code) {
    super();
    this.condition = condition;
    this.code = code;
  }

  public PExpression getCondition() {
    return condition;
  }

  public Block getCode() {
    return code;
  }

  public void setCondition(PExpression condition) {
    this.condition = condition;
  }

  public void setCode(Block code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return "ef";
  }
}
