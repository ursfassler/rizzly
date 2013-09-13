package evl.traverser;

import evl.variable.ConstGlobal;
import java.util.ArrayList;
import java.util.List;

import evl.DefTraverser;
import evl.cfg.BasicBlock;
import evl.composition.ImplComposition;
import evl.hfsm.ImplHfsm;
import evl.other.ImplElementary;
import evl.statement.Statement;

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
      List<Statement> list = visit(stmt, param);
      if (list == null) {
        obj.getCode().add(stmt);
      } else {
        obj.getCode().addAll(list);
      }
    }

    List<Statement> list = visit(obj.getEnd(), param);
    if (list != null) {
      obj.getCode().addAll(list);
    }

    return null;
  }

  // traversal optimization

  @Override
  protected List<Statement> visitImplComposition(ImplComposition obj, T param) {
    return null;
  }

  @Override
  protected List<Statement> visitImplElementary(ImplElementary obj, T param) {
    visitList(obj.getInternalFunction().getList(), param);
    visitList(obj.getInputFunc().getList(), param);
    visitList(obj.getSubComCallback().getList(), param);
    return null;
  }

  @Override
  protected List<Statement> visitImplHfsm(ImplHfsm obj, T param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected List<Statement> visitConstGlobal(ConstGlobal obj, T param) {
    return null;
  }
}
