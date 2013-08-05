/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;


import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import fun.DefGTraverser;
import fun.cfg.BasicBlock;
import fun.cfg.PhiStmt;
import fun.expression.Expression;
import fun.variable.SsaVariable;
import fun.variable.Variable;

public class DefUseKillVisitor extends DefGTraverser<Void, BasicBlock> {
  private HashMap<BasicBlock, HashSet<Variable>> use    = new HashMap<BasicBlock, HashSet<Variable>>();
  private HashMap<BasicBlock, HashSet<Variable>> def    = new HashMap<BasicBlock, HashSet<Variable>>();
  private HashMap<BasicBlock, HashSet<Variable>> kill   = new HashMap<BasicBlock, HashSet<Variable>>();
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
  protected Void visitBasicBlock(BasicBlock obj, BasicBlock param) {
    assert (param == null);

    use.put(obj, new HashSet<Variable>());
    def.put(obj, new HashSet<Variable>());
    kill.put(obj, new HashSet<Variable>());

    super.visitBasicBlock(obj, obj);

//    visitFollowingPhi(obj); //TODO ok?

    return null;
  }

  /* handle phi functions as they belong to the previous basic blocks, what they actually do*/
  private void visitFollowingPhi(BasicBlock bb) {
    for (BasicBlock v : bb.getOutlist()) {
      BbEdge edge = new BbEdge(bb, v);
      Collection<PhiStmt> phis = edge.getDst().getPhis();
      for (PhiStmt phi : phis) {
        Expression expr = phi.getOption().get(bb.getId());
        assert (expr != null);
        visit(expr, bb);
        visit(phi.getVarname(), bb);
      }
    }
  }

  @Override
  protected Void visitPhiStmt(PhiStmt obj, BasicBlock param) {
    return null;
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, BasicBlock param) {
    def.get(param).add(obj.getName());
    kill.get(param).add(obj.getName());
    if (!blocks.containsKey(obj.getName())) {
      blocks.put(obj.getName(), new HashSet<BasicBlock>());
    }
    blocks.get(obj.getName()).add(param);
    return super.visitSsaVariable(obj, param);
  }

  @Override
  protected Void visitVariableRefUnlinked(VariableRefUnlinked obj, BasicBlock param) {
    visitVarRef(obj, param);
    return super.visitVariableRefUnlinked(obj, param);
  }

  @Override
  protected Void visitVariableRefLinked(VariableRefLinked obj, BasicBlock param) {
    visitVarRef(obj, param);
    return super.visitVariableRefLinked(obj, param);
  }

  private void visitVarRef(VariableRef ref, BasicBlock bb) {
    if (!def.get(bb).contains(ref.getName())) {
      use.get(bb).add(ref.getName());
      if (!blocks.containsKey(ref.getName())) {
        blocks.put(ref.getName(), new HashSet<BasicBlock>());
      }
    }
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
