package pir.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pir.PirObject;
import pir.other.SsaVariable;
import pir.other.Variable;

/**
 *
 * @author urs
 */
public class PhiStmt extends PirObject {

  private SsaVariable vardef;
  private Map<BasicBlock, Variable> arg = new HashMap<BasicBlock, Variable>();

  public PhiStmt(SsaVariable vardef) {
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
