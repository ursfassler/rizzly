package pir.statement.phi;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pir.cfg.BasicBlock;
import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.Statement;
import pir.statement.normal.VariableGeneratorStmt;

/**
 * 
 * @author urs
 */
public class PhiStmt extends Statement implements VariableGeneratorStmt {
  private SsaVariable variable;
  final private Map<BasicBlock, PirValue> arg = new HashMap<BasicBlock, PirValue>();

  public PhiStmt(SsaVariable variable) {
    this.variable = variable;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
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
    return variable + " := phi " + arg;
  }

}
