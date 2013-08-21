package evl.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.ElementInfo;

import evl.EvlBase;
import evl.variable.SsaVariable;
import evl.variable.Variable;

/**
 *
 * @author urs
 */
public class PhiStmt extends EvlBase {

  private SsaVariable vardef;
  private Map<BasicBlock, Variable> arg = new HashMap<BasicBlock, Variable>();    //TODO use Reference?

  public PhiStmt(ElementInfo info, SsaVariable vardef) {
    super(info);
    this.vardef = vardef;
  }

  public SsaVariable getVariable() {
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
