package fun.statement;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.Expression;

public class IfOption extends FunBase {
  private Expression condition;
  private Block code;

  public IfOption(ElementInfo info, Expression condition, Block code) {
    super(info);
    this.condition = condition;
    this.code = code;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public Block getCode() {
    return code;
  }

  public void setCode(Block code) {
    this.code = code;
  }

}
