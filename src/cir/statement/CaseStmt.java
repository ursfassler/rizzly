package cir.statement;

import java.util.ArrayList;
import java.util.List;

import cir.expression.Expression;

public class CaseStmt extends Statement {
  private Expression condition;
  final private List<CaseEntry> entries;
  private Statement otherwise;

  public CaseStmt(Expression condition, Statement otherwise) {
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

  public Statement getOtherwise() {
    return otherwise;
  }

  public void setCondition(Expression condition) {
    this.condition = condition;
  }

  public void setOtherwise(Statement otherwise) {
    this.otherwise = otherwise;
  }

  @Override
  public String toString() {
    return "switch( " + condition + " )";
  }

}
