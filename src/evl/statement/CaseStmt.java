package evl.statement;

import java.util.List;

import common.ElementInfo;

import evl.expression.Expression;

public class CaseStmt extends Statement {
  private Expression condition;
  private List<CaseOpt> option;
  private Block otherwise;

  public CaseStmt(ElementInfo info, Expression condition, List<CaseOpt> option, Block otherwise) {
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

  public List<CaseOpt> getOption() {
    return option;
  }

}
