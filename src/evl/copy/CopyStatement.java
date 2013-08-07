package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.statement.VarDefStmt;

public class CopyStatement extends NullTraverser<Statement, Void> {
  private CopyEvl cast;

  public CopyStatement(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Statement visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Statement visitVarDef(VarDefStmt obj, Void param) {
    return new VarDefStmt(obj.getInfo(), cast.copy(obj.getVariable()));
  }

  @Override
  protected Statement visitAssignment(Assignment obj, Void param) {
    return new Assignment(obj.getInfo(), cast.copy(obj.getLeft()), cast.copy(obj.getRight()));
  }

  @Override
  protected Statement visitCallStmt(CallStmt obj, Void param) {
    return new CallStmt(obj.getInfo(), cast.copy(obj.getCall()));
  }

  @Override
  protected Statement visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), cast.copy(obj.getExpr()));
  }

  @Override
  protected Statement visitReturnVoid(ReturnVoid obj, Void param) {
    return new ReturnVoid(obj.getInfo());
  }

}
