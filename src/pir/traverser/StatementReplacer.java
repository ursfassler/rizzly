package pir.traverser;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.statement.normal.NormalStmt;
import pir.statement.phi.PhiStmt;

/**
 * If the return value is null, the statement is kept. If it is != null, the statement is replaced with the content of
 * the list.
 * 
 * @author urs
 * 
 * @param <T>
 */
public class StatementReplacer<T> extends DefTraverser<List<NormalStmt>, T> {

  /**
   * Return a single statement
   * 
   * @param stmt
   * @return
   */
  protected List<NormalStmt> ret(NormalStmt stmt) {
    ArrayList<NormalStmt> stmts = new ArrayList<NormalStmt>();
    stmts.add(stmt);
    return stmts;
  }

  protected void visitPhiOption(PhiStmt phi, BasicBlock in, List<NormalStmt> stmts, T param){
  }

  @Override
  protected List<NormalStmt> visitBasicBlock(BasicBlock obj, T param) {
    // TODO somehow include phi statements

    ArrayList<NormalStmt> stmts = new ArrayList<NormalStmt>(obj.getCode());
    obj.getCode().clear();

    for (NormalStmt stmt : stmts) {
      List<NormalStmt> list = visit(stmt, param);
      if (list == null) {
        obj.getCode().add(stmt);
      } else {
        obj.getCode().addAll(list);
      }
    }

    {
      List<NormalStmt> list = visit(obj.getEnd(), param);
      if (list != null) {
        obj.getCode().addAll(list);
      }
    }

    for (BasicBlock next : obj.getEnd().getJumpDst()) {
      for (PhiStmt phi : next.getPhi()) {
        visitPhiOption(phi, obj, obj.getCode(), param);
      }
    }

    return null;
  }
}
