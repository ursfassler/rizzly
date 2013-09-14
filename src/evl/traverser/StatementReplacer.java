package evl.traverser;

import evl.expression.Expression;
import evl.variable.ConstGlobal;
import java.util.ArrayList;
import java.util.List;

import evl.DefTraverser;
import evl.cfg.BasicBlock;
import evl.composition.ImplComposition;
import evl.hfsm.ImplHfsm;
import evl.other.ImplElementary;
import evl.statement.normal.NormalStmt;
import evl.statement.phi.PhiStmt;

/**
 * If the return value is null, the statement is kept. If it is != null, the statement is replaced with the content of
 * the list.
 * 
 * @author urs
 * 
 * @param <T>
 */
public abstract class StatementReplacer<T> extends DefTraverser<List<NormalStmt>, T> {

  /**
   * Return a single statement
   * @param stmt
   * @return
   */
  protected List<NormalStmt> ret(NormalStmt stmt) {
    ArrayList<NormalStmt> stmts = new ArrayList<NormalStmt>();
    stmts.add(stmt);
    return stmts;
  }

  abstract protected List<NormalStmt> visitPhi(PhiStmt phi, BasicBlock in, T param);
  
  @Override
  protected List<NormalStmt> visitBasicBlock(BasicBlock obj, T param) {
    ArrayList<NormalStmt> stmts = new ArrayList<NormalStmt>(obj.getCode());
    obj.getCode().clear();

    for( NormalStmt stmt : stmts ) {
      List<NormalStmt> list = visit(stmt, param);
      if( list == null ) {
        obj.getCode().add(stmt);
      } else {
        obj.getCode().addAll(list);
      }
    }

    {
      List<NormalStmt> list = visit(obj.getEnd(), param);
      if( list != null ) {
        obj.getCode().addAll(list);
      }
    }

    // go through phi statements of following bbs
    for( BasicBlock dst : obj.getEnd().getJumpDst() ) {
      for( PhiStmt phi : dst.getPhi() ) {
        List<NormalStmt> list = visitPhi( phi, obj, param);
        if( list != null ) {
          obj.getCode().addAll(list);
        }
      }
    }


    return null;
  }

  // traversal optimization
  @Override
  protected List<NormalStmt> visitImplComposition(ImplComposition obj, T param) {
    return null;
  }

  @Override
  protected List<NormalStmt> visitImplElementary(ImplElementary obj, T param) {
    visitList(obj.getInternalFunction().getList(), param);
    visitList(obj.getInputFunc().getList(), param);
    visitList(obj.getSubComCallback().getList(), param);
    return null;
  }

  @Override
  protected List<NormalStmt> visitImplHfsm(ImplHfsm obj, T param) {
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected List<NormalStmt> visitConstGlobal(ConstGlobal obj, T param) {
    return null;
  }

}
