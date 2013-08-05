package fun.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.variable.FuncVariable;
import fun.variable.Variable;

/**
 *
 * @author urs
 */
public class PhiStmt extends FunBase {

  private FuncVariable vardef;
  private Map<BasicBlock, Variable> arg = new HashMap<BasicBlock, Variable>();

  public PhiStmt(ElementInfo info, FuncVariable vardef) {
    super(info);
    this.vardef = vardef;
  }

  public FuncVariable getVardef() {
    return vardef;
  }

  public void addArg(BasicBlock bb, Variable var) {
    arg.put(bb, var);
  }

  public Variable getArg(BasicBlock bb) {
    return arg.get(bb);
  }

  public Set<BasicBlock> getInBB() {
    return new HashSet<BasicBlock>(arg.keySet());
  }

  public Fun getVarname() {
    throw new RuntimeException("not yet implemented");
  }

}
