package evl.copy;

import java.util.ArrayList;

import evl.Evl;
import evl.NullTraverser;
import evl.statement.Assignment;
import evl.statement.Block;
import evl.statement.CallStmt;
import evl.statement.CaseOpt;
import evl.statement.CaseStmt;
import evl.statement.IfStmt;
import evl.statement.ReturnExpr;
import evl.statement.ReturnVoid;
import evl.statement.Statement;
import evl.statement.VarDefStmt;
import evl.statement.WhileStmt;

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
  protected Statement visitBlock(Block obj, Void param) {
    Block ret = new Block(obj.getInfo());
    ret.getStatements().addAll(cast.copy(obj.getStatements()));
    return ret;
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

  @Override
  protected Statement visitCaseStmt(CaseStmt obj, Void param) {
    return new CaseStmt(obj.getInfo(), cast.copy(obj.getCondition()), new ArrayList<CaseOpt>(cast.copy(obj.getOption())), cast.copy(obj.getOtherwise()));
  }

  @Override
  protected Statement visitIfStmt(IfStmt obj, Void param) {
    return new IfStmt(obj.getInfo(), cast.copy(obj.getOption()), cast.copy(obj.getDefblock()));
  }

  @Override
  protected Statement visitWhileStmt(WhileStmt obj, Void param) {
    return new WhileStmt(obj.getInfo(), cast.copy(obj.getCondition()), cast.copy(obj.getBody()));
  }

}
