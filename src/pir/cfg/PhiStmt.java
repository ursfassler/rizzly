package pir.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.VariableGeneratorStmt;

/**
 * 
 * @author urs
 */
public class PhiStmt extends VariableGeneratorStmt {
  private Map<BasicBlock, PirValue> arg = new HashMap<BasicBlock, PirValue>();

  public PhiStmt(SsaVariable variable) {
    super(variable);
  }

  public void addArg(BasicBlock bb, PirValue var) {
    arg.put(bb, var);
  }

  public PirValue getArg(BasicBlock bb) {
    return arg.get(bb);
  }

  public Set<BasicBlock> getInBB() {
    return new HashSet<BasicBlock>(arg.keySet());
  }

  public Set<PirValue> getReferences() {
    return new HashSet<PirValue>(arg.values());
  }

  @Override
  public String toString() {
    return getVariable() + " := phi " + arg;
  }

}
