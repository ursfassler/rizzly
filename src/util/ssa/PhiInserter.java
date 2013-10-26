/*
 * Part of upcompiler. Copyright (c) 2012, Urs Fässler, Licensed under the GNU Genera Public License, v3
 * @author: urs@bitzgi.ch
 */
package util.ssa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Pair;
import evl.cfg.BasicBlock;
import evl.cfg.BasicBlockList;
import evl.statement.phi.PhiStmt;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.variable.FuncVariable;
import evl.variable.SsaVariable;
import evl.variable.Variable;

//FIXME go over textbook again, this algorithm is dirty and may not work correct
@Deprecated
public class PhiInserter {

  private List<Variable> globals = new ArrayList<Variable>();
  private BasicBlockList bbl;
  private List<Variable> arglist;
  private DominanceFrontier<BasicBlock, BbEdge> df;
  private Set<Pair<BasicBlock, Variable>> hasPhi = new HashSet<Pair<BasicBlock, Variable>>();
  private Map<SsaVariable, FuncVariable> renamed = new HashMap<SsaVariable, FuncVariable>();
  private Map<BasicBlock, Set<Variable>> closedUse = new HashMap<BasicBlock, Set<Variable>>();

  public PhiInserter(BasicBlockList bbl, List<Variable> arglist, DominanceFrontier<BasicBlock, BbEdge> df) {
    super();
    this.df = df;
    this.bbl = bbl;
    this.arglist = arglist;
  }

  @Deprecated
  public void doWork() {
    DefUseKillGetter getter = new DefUseKillGetter();
    getter.traverse(bbl, arglist);

    closedUse = new HashMap<BasicBlock, Set<Variable>>(getter.getUse());
    makeClosedUse(closedUse);

    // If a variable is used but never defined it has to be a global variable
    // it does not hold for definitions for variables
    // FIXME why?
    for (Set<Variable> g : getter.getUse().values()) {
      globals.addAll(g);
    }

    // Collections.sort(globals, new VarComp()); // needed to make output deterministic
    int number = 0;

    for (Variable v : globals) {
      if (!(v instanceof FuncVariable)) {
        continue;
      }
      if (!isScalar(v.getType().getRef())) {
        continue;
      }
      FuncVariable x = (FuncVariable) v;
      Set<BasicBlock> blocks = getter.getBlocks().get(x);
      LinkedList<BasicBlock> worklist = new LinkedList<BasicBlock>(blocks);
      for (int i = 0; i < worklist.size(); i++) {
        BasicBlock b = worklist.get(i);
        for (BasicBlock d : df.getDf().get(b)) {
          // FIXME workaround for non-working phi inserter. Redo whole function.
          if (closedUse.get(d).contains(x)) {
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
  }

  static private void makeClosedUse(Map<BasicBlock, Set<Variable>> use) {
    for (BasicBlock bb : use.keySet()) {
      Set<Variable> acuse = use.get(bb);
      ArrayList<BasicBlock> worklist = new ArrayList<BasicBlock>();
      worklist.add(bb);

      for (int i = 0; i < worklist.size(); i++) {
        BasicBlock ab = worklist.get(i);
        acuse.addAll(use.get(ab));
        Set<BasicBlock> next = new HashSet<BasicBlock>(ab.getEnd().getJumpDst());
        next.removeAll(worklist);
        worklist.addAll(next);
      }
    }
  }

  @Deprecated
  public static boolean isScalar(Type type) {
    // TODO make it nice
    return (type instanceof RangeType) || (type instanceof IntegerType) || (type instanceof NaturalType) || (type instanceof EnumType) || (type instanceof BooleanType);
  }

  @Deprecated
  public static boolean isAggregate(Type type) {
    // TODO make it nice
    return (type instanceof RecordType) || (type instanceof UnionType) || (type instanceof ArrayType);
  }

  private void insertPhi(BasicBlock bb, FuncVariable var, int number) {
    hasPhi.add(new Pair<BasicBlock, Variable>(bb, var));
    SsaVariable ssaVar = new SsaVariable(var, number);
    PhiStmt phi = new PhiStmt(var.getInfo(), ssaVar); // FIXME what with arguments of phi statement?
    bb.getPhi().add(phi);
    renamed.put(ssaVar, var);
  }

  private boolean hasPhiFor(BasicBlock bb, Variable var) {
    return hasPhi.contains(new Pair<BasicBlock, Variable>(bb, var));
  }

  public Map<SsaVariable, FuncVariable> getRenamed() {
    return renamed;
  }
}
