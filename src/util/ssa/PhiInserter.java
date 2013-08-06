/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import util.Pair;
import evl.cfg.PhiStmt;
import evl.variable.SsaVariable;
import fun.cfg.BasicBlock;
import fun.function.FunctionHeader;
import fun.variable.FuncVariable;
import fun.variable.Variable;

public class PhiInserter {
  private List<Variable> globals = new ArrayList<Variable>();
  private FunctionHeader func;
  private DominanceFrontier<BasicBlock, BbEdge> df;
  private Set<Pair<BasicBlock, Variable>> hasPhi = new HashSet<Pair<BasicBlock, Variable>>();

  public PhiInserter(FunctionHeader func, DominanceFrontier<BasicBlock, BbEdge> df) {
    super();
    this.df = df;
    this.func = func;
  }

  public void doWork() {
    DefUseKillVisitor visitor = new DefUseKillVisitor();
    visitor.traverse(func, null);

    // If a variable is used but never defined it has to be a global variable
    // it does not hold for definitions for variables
    for (Set<Variable> g : visitor.getUse().values()) {
      globals.addAll(g);
    }

    // Collections.sort(globals, new VarComp()); // needed to make output deterministic
    int number = 0;

    for (Variable x : globals) {
      Set<BasicBlock> blocks = visitor.getBlocks().get(x);
      LinkedList<BasicBlock> worklist = new LinkedList<BasicBlock>(blocks);
      for (int i = 0; i < worklist.size(); i++) {
        BasicBlock b = worklist.get(i);
        for (BasicBlock d : df.getDf().get(b)) {
          if (!hasPhiFor(d, x)) {
            number--;
            insertPhi(d, x, number);
            if (!worklist.contains(d)) {
              worklist.add(d);
            }
          }
        }
      }
    }
  }

  private void insertPhi(BasicBlock bb, Variable var, int number) {
    hasPhi.add(new Pair<BasicBlock, Variable>(bb, var));
    SsaVariable ssaVar = new SsaVariable(var,number);
    PhiStmt phi = new PhiStmt(var.getInfo(), ssaVar); // FIXME what with arguments of phi statement?
    bb.getPhi().add(phi);
  }

  private boolean hasPhiFor(BasicBlock bb, Variable var) {
    return hasPhi.contains(new Pair<BasicBlock, Variable>(bb, var));
  }

}
