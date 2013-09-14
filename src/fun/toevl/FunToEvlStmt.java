package fun.toevl;

import java.util.Map;

import evl.Evl;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.statement.Statement;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.Assignment;
import fun.statement.CallStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;

public class FunToEvlStmt extends NullTraverser<Evl, Void> {
  private Map<Fun, Evl> map;
  private FunToEvl fta;

  public FunToEvlStmt(FunToEvl fta, Map<Fun, Evl> map) {
    super();
    this.map = map;
    this.fta = fta;
  }

  @Override
  protected Evl visit(Fun obj, Void param) {
    Evl cobj = (Evl) map.get(obj);
    if (cobj == null) {
      cobj = super.visit(obj, param);
      assert (cobj != null);
      map.put(obj, cobj);
    }
    return cobj;
  }

  @Override
  protected Statement visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  // ----------------------------------------------------------------------------

  @Override
  protected Statement visitAssignment(Assignment obj, Void param) {
    return new evl.statement.normal.Assignment(obj.getInfo(), (evl.expression.reference.Reference) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
  }

  @Override
  protected Evl visitReturnExpr(ReturnExpr obj, Void param) {
    return new evl.statement.bbend.ReturnExpr(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
  }

  @Override
  protected Evl visitReturnVoid(ReturnVoid obj, Void param) {
    return new evl.statement.bbend.ReturnVoid(obj.getInfo());
  }

  @Override
  protected Statement visitVarDef(VarDefStmt obj, Void param) {
    return new evl.statement.normal.VarDefStmt(obj.getInfo(), (evl.variable.FuncVariable) fta.traverse(obj.getVariable(), null));
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, Void param) {
    return new evl.statement.normal.CallStmt(obj.getInfo(), (Reference) fta.traverse(obj.getCall(), null));
  }

}
