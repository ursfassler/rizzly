/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */
package util.ssa;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import evl.DefTraverser;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.expression.reference.Reference;
import evl.statement.normal.Assignment;
import evl.statement.normal.VarDefStmt;
import evl.statement.phi.PhiStmt;
import evl.variable.Variable;
import java.util.List;

public class DefUseKillGetter {

  private DefUseKillVisitor dukvv = new DefUseKillVisitor();

  public HashMap<BasicBlock, HashSet<Variable>> getUse() {
    return dukvv.use;
  }

  public HashMap<BasicBlock, HashSet<Variable>> getDef() {
    return dukvv.def;
  }

  public HashMap<BasicBlock, HashSet<Variable>> getKill() {
    return dukvv.kill;
  }

  public HashMap<Variable, HashSet<BasicBlock>> getBlocks() {
    return dukvv.blocks;
  }

  public void traverse(BasicBlockList bbl, List<Variable> param) {
    // TODO ok? no, move it to function creation
    // we add the argument variables to the first basic block
    BasicBlock start = bbl.getEntry();
    dukvv.use.put(start, new HashSet<Variable>());
    dukvv.def.put(start, new HashSet<Variable>());
    dukvv.kill.put(start, new HashSet<Variable>());

    for( Variable var : param ) {
      dukvv.traverse(var, start);
    }

    dukvv.traverse(bbl, null);
  }
}

class DefUseKillVisitor extends DefTraverser<Void, BasicBlock> {

  HashMap<BasicBlock, HashSet<Variable>> use = new HashMap<BasicBlock, HashSet<Variable>>();
  HashMap<BasicBlock, HashSet<Variable>> def = new HashMap<BasicBlock, HashSet<Variable>>();
  HashMap<BasicBlock, HashSet<Variable>> kill = new HashMap<BasicBlock, HashSet<Variable>>();
  HashMap<Variable, HashSet<BasicBlock>> blocks = new HashMap<Variable, HashSet<BasicBlock>>();

  @Override
  protected Void visitBasicBlock(BasicBlock obj, BasicBlock param) {
    assert ( param == null );

    use.put(obj, new HashSet<Variable>());
    def.put(obj, new HashSet<Variable>());
    kill.put(obj, new HashSet<Variable>());

    super.visitBasicBlock(obj, obj);

    visitFollowingPhi(obj); // TODO ok?

    return null;
  }

  /* handle phi functions as they belong to the previous basic blocks, what they actually do */
  private void visitFollowingPhi(BasicBlock bb) {
    for( BasicBlock v : bb.getEnd().getJumpDst() ) {
      BbEdge edge = new BbEdge(bb, v);
      Collection<PhiStmt> phis = edge.getDst().getPhi();
      for( PhiStmt phi : phis ) {
        throw new RuntimeException("not yet implemented");
        // Expression expr = phi.getArg(bb);
        // assert (expr != null);
        // visit(expr, bb);
        // visit(phi.getVarname(), bb);
      }
    }
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, BasicBlock param) {
    return super.visitPhiStmt(obj, param);
  }

  @Override
  protected Void visitVariable(Variable obj, BasicBlock param) {
    assert ( param != null );
    def.get(param).add(obj);
    kill.get(param).add(obj);
    if( !blocks.containsKey(obj) ) {
      blocks.put(obj, new HashSet<BasicBlock>());
    }
    blocks.get(obj).add(param);
    return super.visitVariable(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, BasicBlock param) {
    if( obj.getLink() instanceof Variable ) {
      visitVarRef((Variable) obj.getLink(), param);
    }
    return super.visitReference(obj, param);
  }

  private void visitVarRef(Variable ref, BasicBlock bb) {
    if( !def.get(bb).contains(ref) ) {
      use.get(bb).add(ref);
      if( !blocks.containsKey(ref) ) {
        blocks.put(ref, new HashSet<BasicBlock>());
      }
    }
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, BasicBlock param) {
    return null;
  }

  @Override
  protected Void visitAssignment(Assignment obj, BasicBlock param) {
    Reference ref = obj.getLeft();
    if( ref.getLink() instanceof Variable ) {
      visitVariable((Variable) ref.getLink(), param);
    }
    visitItr(obj.getLeft().getOffset(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @SuppressWarnings("unused")
  private void print(BasicBlock obj, PrintStream st) {
    st.println(obj);
    st.print("def: ");
    printVarList(def.get(obj), st);
    st.print("use: ");
    printVarList(use.get(obj), st);
    st.print("kill: ");
    printVarList(kill.get(obj), st);
  }

  private void printVarList(Collection<Variable> vars, PrintStream st) {
    for( Variable var : vars ) {
      st.print(var);
      st.print(", ");
    }
    st.println();
  }
}
