package evl.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.ElementInfo;

import evl.EvlBase;
import evl.variable.Variable;

/**
 *
 * @author urs
 */
public class PhiStmt extends EvlBase {

  private Variable vardef;
  private Map<BasicBlock, Variable> arg = new HashMap<BasicBlock, Variable>();

  public PhiStmt(ElementInfo info, Variable vardef) {
    super(info);
    this.vardef = vardef;
  }

  public Variable getVardef() {
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

}
