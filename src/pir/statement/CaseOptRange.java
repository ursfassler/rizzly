package pir.statement;

import pir.expression.PExpression;

final public class CaseOptRange extends CaseOptEntry {
  private PExpression start;
  private PExpression end;

  public CaseOptRange(PExpression start, PExpression end) {
    super();
    this.start = start;
    this.end = end;
  }

  public PExpression getStart() {
    return start;
  }

  public PExpression getEnd() {
    return end;
  }

  public void setStart(PExpression start) {
    this.start = start;
  }

  public void setEnd(PExpression end) {
    this.end = end;
  }

}
