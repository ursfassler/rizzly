package pir.statement;

import java.util.ArrayList;
import java.util.List;

import pir.expression.PExpression;


public class CaseStmt extends Statement {
  private PExpression condition;
  final private List<CaseEntry> entries;
  final private Block otherwise;

  public CaseStmt(PExpression condition, Block otherwise) {
    super();
    this.condition = condition;
    this.entries = new ArrayList<CaseEntry>();
    this.otherwise = otherwise;
  }

  public PExpression getCondition() {
    return condition;
  }

  public List<CaseEntry> getEntries() {
    return entries;
  }

  public Block getOtherwise() {
    return otherwise;
  }

  public void setCondition(PExpression condition) {
    this.condition = condition;
  }

}
