package fun.toevl;

import java.util.Map;

import evl.Evl;
import evl.expression.Expression;
import evl.statement.CaseOptEntry;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.CaseOptRange;
import fun.statement.CaseOptValue;

public class FunToEvlCaseOptEntry extends NullTraverser<CaseOptEntry, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlCaseOptEntry(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected CaseOptEntry visit(Fun obj, Void param) {
    CaseOptEntry cobj = (CaseOptEntry) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected CaseOptEntry visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected CaseOptEntry visitCaseOptRange(CaseOptRange obj, Void param) {
    return new evl.statement.CaseOptRange(obj.getInfo(), (Expression) fta.traverse(obj.getStart(), null), (Expression) fta.traverse(obj.getEnd(), null));
  }

  @Override
  protected CaseOptEntry visitCaseOptValue(CaseOptValue obj, Void param) {
    return new evl.statement.CaseOptValue(obj.getInfo(), (Expression) fta.traverse(obj.getValue(), null));
  }

}
