package pir.traverser;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.statement.Statement;

/**
 * If the return value is null, the statement is kept. If it is != null, the statement is replaced with the content of
 * the list.
 * 
 * @author urs
 * 
 * @param <T>
 */
public class StatementReplacer<T> extends DefTraverser<List<Statement>, T> {

  /**
   * Return a single statement
   * @param stmt
   * @return
   */
  protected List<Statement> ret(Statement stmt) {
    ArrayList<Statement> stmts = new ArrayList<Statement>();
    stmts.add(stmt);
    return stmts;
  }

  @Override
  protected List<Statement> visitBasicBlock(BasicBlock obj, T param) {
    // TODO somehow include phi statements

    ArrayList<Statement> stmts = new ArrayList<Statement>(obj.getCode());
    obj.getCode().clear();

    for (Statement stmt : stmts) {
      List<Statement> list = visit(stmt, null);
      if (list == null) {
        obj.getCode().add(stmt);
      } else {
        obj.getCode().addAll(list);
      }
    }

    List<Statement> list = visit(obj.getEnd(), null);
    if (list != null) {
      obj.getCode().addAll(list);
    }

    return null;
  }

}
