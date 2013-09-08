package pir.statement;

import java.util.ArrayList;
import java.util.List;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class GetElementPtr extends VariableGeneratorStmt {

  private PirValue base;
  final private List<PirValue> offset = new ArrayList<PirValue>();

  public GetElementPtr(SsaVariable variable, PirValue base, List<PirValue> offset) {
    super(variable);
    this.base = base;
    this.offset.addAll(offset);
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
    return super.toString() + " := getelementptr " + base + offset;
  }
}
