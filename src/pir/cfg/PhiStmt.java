package pir.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pir.expression.reference.VarRef;
import pir.other.SsaVariable;
import pir.statement.Statement;

/**
 * 
 * @author urs
 */
public class PhiStmt extends Statement {

  private SsaVariable vardef;
  private Map<BasicBlock, VarRef> arg = new HashMap<BasicBlock, VarRef>();

  public PhiStmt(SsaVariable vardef) {
    this.vardef = vardef;
  }

  public SsaVariable getVariable() {
    return vardef;
  }

  public void addArg(BasicBlock bb, VarRef var) {
    arg.put(bb, var);
  }

  public VarRef getArg(BasicBlock bb) {
    return arg.get(bb);
  }

  public Set<BasicBlock> getInBB() {
    return new HashSet<BasicBlock>(arg.keySet());
  }

  public Set<VarRef> getReferences() {
    return new HashSet<VarRef>(arg.values());
  }

  @Override
  public String toString() {
    return getVariable() + " := phi " + arg;
  }

}
