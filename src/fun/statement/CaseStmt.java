package fun.statement;

import common.ElementInfo;

import fun.expression.Expression;
import fun.other.FunList;

public class CaseStmt extends Statement {
  private Expression condition;
  private FunList<CaseOpt> option;
  private Block otherwise;

  public CaseStmt(ElementInfo info, Expression condition, FunList<CaseOpt> option, Block otherwise) {
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

  public FunList<CaseOpt> getOption() {
    return option;
  }

}
