package evl.traverser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.NullTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.Goto;
import evl.cfg.PhiStmt;
import evl.cfg.ReturnExpr;
import evl.cfg.ReturnVoid;
import evl.expression.reference.Reference;
import evl.statement.VarDefInitStmt;
import evl.variable.SsaVariable;

public class VariableReplacer extends NullTraverser<Boolean, Void> {
  final private Set<BasicBlock> checked = new HashSet<BasicBlock>();
  private ExprVarRepl exprVarRepl;

  public VariableReplacer(SsaVariable old, SsaVariable replacement) {
    super();
    exprVarRepl = new ExprVarRepl(old, replacement);
  }

  /**
   * Replaces all usages of the variable old with replacement. It starts at the basic block rootBb with the statement at
   * index start. It follows links to other basic blocks. It ends if there is no more statements or basic block OR if a
   * statement defines the variable old.
   * 
   * @param rootBb
   * @param start
   * @param old
   * @param replacement
   */
  public static void replace(BasicBlock rootBb, int start, SsaVariable old, SsaVariable replacement) {
    VariableReplacer replacer = new VariableReplacer(old, replacement);
    replacer.start(rootBb, start);
  }

  private void start(BasicBlock rootBb, int start) {
    for (int i = start; i < rootBb.getCode().size(); i++) {
      if (!visit(rootBb.getCode().get(i), null)) {
        return;
      }
    }
    visit(rootBb.getEnd(), null);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitBasicBlock(BasicBlock obj, Void param) {
    if (checked.contains(obj)) {
      return null;
    }

    ArrayList<Evl> list = new ArrayList<Evl>();
    list.addAll(obj.getPhi());
    list.addAll(obj.getCode());

    for (Evl stmt : list) {
      if (!visit(stmt, null)) {
        return null;
      }
    }
    
    visit(obj.getEnd(), null);

    checked.add(obj);
    return null;
  }

  @Override
  protected Boolean visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    exprVarRepl.traverse(obj.getInit(), null);
    return obj.getVariable() != exprVarRepl.getOld();
  }

  @Override
  protected Boolean visitPhiStmt(PhiStmt obj, Void param) {
    //TODO use exprVarRepl?
    for( BasicBlock in : new ArrayList<BasicBlock>( obj.getInBB()) ){
      if( obj.getArg(in) == exprVarRepl.getOld() ){
        obj.addArg(in, exprVarRepl.getReplacement());
      }
    }
    return obj.getVariable() != exprVarRepl.getOld();
  }

  @Override
  protected Boolean visitGoto(Goto obj, Void param) {
    visit(obj.getTarget(), null);
    return null;
  }

  @Override
  protected Boolean visitReturnExpr(ReturnExpr obj, Void param) {
    exprVarRepl.traverse(obj.getExpr(), null);
    return null;
  }

  @Override
  protected Boolean visitReturnVoid(ReturnVoid obj, Void param) {
    return null;
  }

  
  
}

class ExprVarRepl extends DefTraverser<Void, Void> {
  final private SsaVariable old;
  final private SsaVariable replacement;

  public ExprVarRepl(SsaVariable old, SsaVariable replacement) {
    super();
    this.old = old;
    this.replacement = replacement;
  }

  public SsaVariable getOld() {
    return old;
  }

  public SsaVariable getReplacement() {
    return replacement;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() == old) {
      obj.setLink(replacement);
    }
    return super.visitReference(obj, param);
  }

}
