package pir.statement.normal;

import java.util.ArrayList;
import java.util.List;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class GetElementPtr extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;
  private PirValue base;
  final private List<PirValue> offset = new ArrayList<PirValue>();

  public GetElementPtr(SsaVariable variable, PirValue base, List<PirValue> offset) {
    this.variable = variable;
    this.base = base;
    this.offset.addAll(offset);
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  public PirValue getBase() {
    return base;
  }

  public void setBase(PirValue base) {
    this.base = base;
  }

  public List<PirValue> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return variable + " := getelementptr " + base + offset;
  }
}
