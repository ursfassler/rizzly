package evl.statement;

import common.ElementInfo;

import evl.expression.Expression;

public class CaseOptRange extends CaseOptEntry {
  private Expression start;
  private Expression end;

  public CaseOptRange(ElementInfo info, Expression start, Expression end) {
    super(info);
    this.start = start;
    this.end = end;
  }

  public Expression getStart() {
    return start;
  }

  public void setStart(Expression start) {
    this.start = start;
  }

  public Expression getEnd() {
    return end;
  }

  public void setEnd(Expression end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return start.toString() + ".." + end.toString();
  }

}
