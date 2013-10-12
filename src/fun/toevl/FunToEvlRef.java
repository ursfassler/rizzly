package fun.toevl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.expression.Expression;
import evl.expression.reference.RefItem;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.reference.RefCall;
import fun.expression.reference.RefTemplCall;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefName;

public class FunToEvlRef extends NullTraverser<RefItem, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlRef(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected RefItem visit(Fun obj, Void param) {
    RefItem cobj = (RefItem) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected RefItem visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  @Override
  protected RefItem visitRefCompcall(RefTemplCall obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "Unresolved compcall");
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected RefItem visitRefCall(RefCall obj, Void param) {
    List<Expression> ap = new ArrayList<Expression>();
    for (fun.expression.Expression expr : obj.getActualParameter()) {
      ap.add((Expression) fta.traverse(expr, null));
    }
    return new evl.expression.reference.RefCall(obj.getInfo(), ap);
  }

  @Override
  protected RefItem visitRefName(RefName obj, Void param) {
    return new evl.expression.reference.RefName(obj.getInfo(), obj.getName());
  }

  @Override
  protected RefItem visitRefIndex(RefIndex obj, Void param) {
    return new evl.expression.reference.RefIndex(obj.getInfo(), (Expression) fta.traverse(obj.getIndex(), null));
  }

}
