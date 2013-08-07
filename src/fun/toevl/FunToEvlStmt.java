package fun.toevl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.cfg.BasicBlock;
import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.statement.Statement;
import fun.Fun;
import fun.NullTraverser;
import fun.statement.Assignment;
import fun.statement.Block;
import fun.statement.CallStmt;
import fun.statement.CaseOpt;
import fun.statement.CaseStmt;
import fun.statement.IfOption;
import fun.statement.IfStmt;
import fun.statement.ReturnExpr;
import fun.statement.ReturnVoid;
import fun.statement.VarDefStmt;
import fun.statement.While;

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
    return new evl.statement.Assignment(obj.getInfo(), (evl.expression.reference.Reference) fta.traverse(obj.getLeft(), null), (Expression) fta.traverse(obj.getRight(), null));
  }

  @Override
  protected Evl visitReturnExpr(ReturnExpr obj, Void param) {
    return new evl.cfg.ReturnExpr(obj.getInfo(), (Expression) fta.traverse(obj.getExpr(), null));
  }

  @Override
  protected Evl visitReturnVoid(ReturnVoid obj, Void param) {
    return new evl.cfg.ReturnVoid(obj.getInfo());
  }

  @Override
  protected Statement visitVarDef(VarDefStmt obj, Void param) {
    return new evl.statement.VarDefStmt(obj.getInfo(), (evl.variable.FuncVariable) fta.traverse(obj.getVariable(), null));
  }

  @Override
  protected Statement visitWhile(While obj, Void param) {
    return new evl.statement.While(obj.getInfo(), (Expression) fta.traverse(obj.getCondition(), null), (evl.statement.Block) visit(obj.getBody(), null));
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, Void param) {
    return new evl.statement.CallStmt(obj.getInfo(), (Reference) fta.traverse(obj.getCall(), null));
  }

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, Void param) {
    List<evl.statement.CaseOpt> opt = new ArrayList<evl.statement.CaseOpt>();
    for (CaseOpt itr : obj.getOption()) {
      opt.add((evl.statement.CaseOpt) fta.traverse(itr, null));
    }
    return new evl.statement.CaseStmt(obj.getInfo(), (Expression) fta.traverse(obj.getCondition(), null), opt, (evl.statement.Block) fta.traverse(obj.getOtherwise(), null));
  }

  @Override
  protected Statement visitIfStmt(IfStmt obj, Void param) {
    List<evl.statement.IfOption> opt = new ArrayList<evl.statement.IfOption>();
    for (IfOption itr : obj.getOption()) {
      evl.statement.IfOption nopt = new evl.statement.IfOption(obj.getInfo(), (Expression) fta.traverse(itr.getCondition(), null), (evl.statement.Block) fta.traverse(itr.getCode(), null));
      opt.add(nopt);
    }

    return new evl.statement.IfStmt(obj.getInfo(), opt, (evl.statement.Block) fta.traverse(obj.getDefblock(), null));
  }

}
