package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.EvlList;

public class CaseStmt extends Statement {
  private Expression condition;
  private EvlList<CaseOpt> option;
  private Block otherwise;

  public CaseStmt(ElementInfo info, Expression condition, EvlList<CaseOpt> option, Block otherwise) {
    super(info);
    this.condition = condition;
    this.option = option;
    this.otherwise = otherwise;
  }

  public Expression getCondition() {
    return condition;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public Block getOtherwise() {
    return otherwise;
  }

  public void setOtherwise(Block otherwise) {
    this.otherwise = otherwise;
  }

  public EvlList<CaseOpt> getOption() {
    return option;
  }

}
