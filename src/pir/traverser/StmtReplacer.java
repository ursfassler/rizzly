package pir.traverser;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.function.Function;
import pir.other.Program;
import pir.statement.bbend.BasicBlockEnd;
import pir.statement.normal.NormalStmt;

//TODO check if ok when only handle NormalStmt

public class StmtReplacer<P> extends DefTraverser<List<NormalStmt>, P> {

  protected List<NormalStmt> add(NormalStmt stmt) {
    ArrayList<NormalStmt> ret = new ArrayList<NormalStmt>();
    ret.add(stmt);
    return ret;
  }

  protected List<NormalStmt> add(NormalStmt stmt1, NormalStmt stmt2) {
    ArrayList<NormalStmt> ret = new ArrayList<NormalStmt>();
    ret.add(stmt1);
    ret.add(stmt2);
    return ret;
  }

  @Override
  protected List<NormalStmt> visitProgram(Program obj, P param) {
    for( Function func : obj.getFunction() ) {
      visit(func, param);
    }
    return null;
  }

  @Override
  protected List<NormalStmt> visitBasicBlock(BasicBlock obj, P param) {
    ArrayList<NormalStmt> stmts = new ArrayList<NormalStmt>(obj.getCode());
    obj.getCode().clear();

    for( NormalStmt stmt : stmts ) {
      List<NormalStmt> list = visit(stmt, param);
      obj.getCode().addAll(list);
    }

    List<NormalStmt> list = visit(obj.getEnd(), param);
    obj.getCode().addAll(list);

    return null;
  }

  @Override
  protected List<NormalStmt> visitBasicBlockEnd(BasicBlockEnd obj, P param) {
    List<NormalStmt> ret = super.visitBasicBlockEnd(obj, param);
    if( ret == null ) {
      ret = new ArrayList<NormalStmt>();
    }
    return ret;
  }

  @Override
  protected List<NormalStmt> visitNormalStmt(NormalStmt obj, P param) {
    List<NormalStmt> ret = super.visitNormalStmt(obj, param);
    if( ret == null ) {
      ret = add(obj);
    }
    return ret;
  }
}
