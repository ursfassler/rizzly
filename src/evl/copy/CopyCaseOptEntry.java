package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.statement.CaseOptEntry;
import evl.statement.CaseOptRange;
import evl.statement.CaseOptValue;

public class CopyCaseOptEntry extends NullTraverser<CaseOptEntry, Void> {
  private CopyEvl cast;

  public CopyCaseOptEntry(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected CaseOptEntry visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CaseOptEntry visitCaseOptValue(CaseOptValue obj, Void param) {
    return new CaseOptValue(obj.getInfo(), cast.copy(obj.getValue()));
  }

  @Override
  protected CaseOptEntry visitCaseOptRange(CaseOptRange obj, Void param) {
    return new CaseOptRange(obj.getInfo(), cast.copy(obj.getStart()),cast.copy(obj.getEnd()));
  }

}
