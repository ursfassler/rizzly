/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */

package util.ssa;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fun.cfg.BasicBlock;
import fun.function.FunctionHeader;
import fun.variable.SsaVariable;
import fun.variable.Variable;

public class PhiInserter {
  private List<Variable> globals = new ArrayList<Variable>();
  private FunctionHeader func;
  private DominanceFrontier<BasicBlock, BbEdge> df;

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
          if (!d.hasPhiFor(x)) {
            number--;
            d.insertPhi(new SsaVariable(x, number));
            if (!worklist.contains(d)) {
              worklist.add(d);
            }
          }
        }
      }
    }
  }

}
