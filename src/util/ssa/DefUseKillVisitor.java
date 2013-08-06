/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import evl.cfg.BasicBlockList;
import evl.cfg.PhiStmt;
import fun.DefGTraverser;
import fun.cfg.BasicBlock;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.function.FuncWithBody;
import fun.function.FunctionHeader;
import fun.statement.Assignment;
import fun.statement.VarDefStmt;
import fun.variable.Variable;

public class DefUseKillVisitor extends DefGTraverser<Void, BasicBlock> {
  private HashMap<BasicBlock, HashSet<Variable>> use = new HashMap<BasicBlock, HashSet<Variable>>();
  private HashMap<BasicBlock, HashSet<Variable>> def = new HashMap<BasicBlock, HashSet<Variable>>();
  private HashMap<BasicBlock, HashSet<Variable>> kill = new HashMap<BasicBlock, HashSet<Variable>>();
  private HashMap<Variable, HashSet<BasicBlock>> blocks = new HashMap<Variable, HashSet<BasicBlock>>();

  public HashMap<BasicBlock, HashSet<Variable>> getUse() {
    return use;
  }

  public HashMap<BasicBlock, HashSet<Variable>> getDef() {
    return def;
  }

  public HashMap<BasicBlock, HashSet<Variable>> getKill() {
    return kill;
  }

  public HashMap<Variable, HashSet<BasicBlock>> getBlocks() {
    return blocks;
  }

  @Override
  protected Void visitFunctionHeader(FunctionHeader obj, BasicBlock param) {
    assert (param == null);

    if (obj instanceof FuncWithBody) {
      BasicBlockList bbl = (BasicBlockList) ((FuncWithBody) obj).getBody();
      // TODO ok?
      // we add the argument variables to the first basic block
      param = bbl.getBasicBlocks().get(0);
      use.put(param, new HashSet<Variable>());
      def.put(param, new HashSet<Variable>());
      kill.put(param, new HashSet<Variable>());
      visitItr(obj.getParam(), param);

      visit(bbl, null);
    }

    return null;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, BasicBlock param) {
    assert (param == null);

    use.put(obj, new HashSet<Variable>());
    def.put(obj, new HashSet<Variable>());
    kill.put(obj, new HashSet<Variable>());

    super.visitBasicBlock(obj, obj);

    visitFollowingPhi(obj); // TODO ok?

    return null;
  }

  /* handle phi functions as they belong to the previous basic blocks, what they actually do */
  private void visitFollowingPhi(BasicBlock bb) {
    for (BasicBlock v : bb.getEnd().getJumpDst()) {
      BbEdge edge = new BbEdge(bb, v);
      Collection<PhiStmt> phis = edge.getDst().getPhi();
      for (PhiStmt phi : phis) {
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
    assert (param != null);
    def.get(param).add(obj);
    kill.get(param).add(obj);
    if (!blocks.containsKey(obj)) {
      blocks.put(obj, new HashSet<BasicBlock>());
    }
    blocks.get(obj).add(param);
    return super.visitVariable(obj, param);
  }

  @Override
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, BasicBlock param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, BasicBlock param) {
    if ((obj.getLink() instanceof Variable) && (obj.getOffset().isEmpty())) {
      visitVarRef((Variable) obj.getLink(), param);
    }
    return super.visitReferenceLinked(obj, param);
  }

  private void visitVarRef(Variable ref, BasicBlock bb) {
    if (!def.get(bb).contains(ref)) {
      use.get(bb).add(ref);
      if (!blocks.containsKey(ref)) {
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
    ReferenceLinked ref = (ReferenceLinked) obj.getLeft();
    if ((ref.getLink() instanceof Variable) && (ref.getOffset().isEmpty())) {
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
    for (Variable var : vars) {
      st.print(var);
      st.print(", ");
    }
    st.println();
  }

}
