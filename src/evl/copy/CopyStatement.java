package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.statement.bbend.CaseGoto;
import evl.statement.bbend.Goto;
import evl.statement.bbend.IfGoto;
import evl.statement.bbend.ReturnExpr;
import evl.statement.bbend.ReturnVoid;
import evl.statement.normal.CallStmt;
import evl.statement.normal.GetElementPtr;
import evl.statement.normal.LoadStmt;
import evl.statement.normal.StackMemoryAlloc;
import evl.statement.Statement;
import evl.statement.normal.Assignment;
import evl.statement.normal.StoreStmt;
import evl.statement.normal.VarDefInitStmt;
import evl.statement.normal.VarDefStmt;
import evl.statement.phi.PhiStmt;

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

  @Override
  protected Statement visitStackMemoryAlloc(StackMemoryAlloc obj, Void param) {
    return new StackMemoryAlloc(obj.getInfo(), cast.copy(obj.getVariable()));
  }

  @Override
  protected Statement visitCaseGoto(CaseGoto obj, Void param) {
    CaseGoto ret = new CaseGoto(obj.getInfo());
    ret.setCondition(cast.copy(obj.getCondition()));
    ret.getOption().addAll(cast.copy(obj.getOption()));
    ret.setOtherwise(cast.copy(obj.getOtherwise()));
    return ret;
  }

  @Override
  protected Statement visitIfGoto(IfGoto obj, Void param) {
    IfGoto ret = new IfGoto(obj.getInfo());
    ret.setCondition(cast.copy(obj.getCondition()));
    ret.setThenBlock(cast.copy(obj.getThenBlock()));
    ret.setElseBlock(cast.copy(obj.getElseBlock()));
    return ret;
  }

  @Override
  protected Statement visitGoto(Goto obj, Void param) {
    return new Goto(obj.getInfo(), cast.copy(obj.getTarget()));
  }

  @Override
  protected Statement visitReturnExpr(ReturnExpr obj, Void param) {
    return new ReturnExpr(obj.getInfo(), cast.copy(obj.getExpr()));
  }

  @Override
  protected Statement visitReturnVoid(ReturnVoid obj, Void param) {
    return new ReturnVoid(obj.getInfo());
  }

  @Override
  protected Statement visitPhiStmt(PhiStmt obj, Void param) {
    PhiStmt ret = new PhiStmt(obj.getInfo(), cast.copy(obj.getVariable()));
    for (BasicBlock in : obj.getInBB()) {
      ret.addArg(cast.copy(in), cast.copy(obj.getArg(in)));
    }
    return ret;
  }

}
