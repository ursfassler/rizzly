package fun.toevl;

import evl.expression.Expression;
import evl.statement.bbend.CaseOptEntry;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;

public class FunToEvlCaseOptEntry extends NullTraverser<CaseOptEntry, Void> {
  private FunToEvl fta;

  public FunToEvlCaseOptEntry(FunToEvl fta) {
    super();
    this.fta = fta;
  }

  @Override
  protected CaseOptEntry visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CaseOptEntry visitCaseOptRange(CaseOptRange obj, Void param) {
    return new evl.statement.bbend.CaseOptRange(obj.getInfo(), (Expression) fta.traverse(obj.getStart(), null), (Expression) fta.traverse(obj.getEnd(), null));
  }

  @Override
  protected CaseOptEntry visitCaseOptValue(CaseOptValue obj, Void param) {
    return new evl.statement.bbend.CaseOptValue(obj.getInfo(), (Expression) fta.traverse(obj.getValue(), null));
  }

}
