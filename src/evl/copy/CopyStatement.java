package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.statement.Assignment;
import evl.statement.CallStmt;
import evl.statement.GetElementPtr;
import evl.statement.LoadStmt;
import evl.statement.Statement;
import evl.statement.StoreStmt;
import evl.statement.VarDefInitStmt;
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
  protected Statement visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    return new VarDefInitStmt(obj.getInfo(), cast.copy(obj.getVariable()), cast.copy(obj.getInit()));
  }

  @Override
  protected Statement visitLoadStmt(LoadStmt obj, Void param) {
    return new LoadStmt(obj.getInfo(), cast.copy(obj.getVariable()), cast.copy(obj.getAddress()));
  }

  @Override
  protected Statement visitStoreStmt(StoreStmt obj, Void param) {
    return new StoreStmt(obj.getInfo(), cast.copy(obj.getAddress()), cast.copy(obj.getExpr()));
  }

  @Override
  protected Statement visitGetElementPtr(GetElementPtr obj, Void param) {
    return new GetElementPtr(obj.getInfo(), cast.copy(obj.getVariable()), cast.copy(obj.getAddress()));
  }

}
