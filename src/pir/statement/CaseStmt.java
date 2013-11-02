package pir.statement;

import java.util.ArrayList;
import java.util.List;

import pir.expression.Expression;


public class CaseStmt extends Statement {
  private Expression condition;
  final private List<CaseEntry> entries;
  final private Block otherwise;

  public CaseStmt(Expression condition, Block otherwise) {
    super();
    this.condition = condition;
    this.entries = new ArrayList<CaseEntry>();
    this.otherwise = otherwise;
  }

  public Expression getCondition() {
    return condition;
  }

  public List<CaseEntry> getEntries() {
    return entries;
  }

  public Block getOtherwise() {
    return otherwise;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

}
