package pir.statement;

import java.util.List;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class Insertvalue extends VariableGeneratorStmt {
  private PirValue old;
  private PirValue src;
  final private List<Integer> index;

  public Insertvalue(SsaVariable dst, PirValue old, PirValue src, List<Integer> index) {
    super(dst);
    this.old = old;
    this.src = src;
    this.index = index;
  }

  public PirValue getSrc() {
    return src;
  }

  public void setSrc(PirValue src) {
    this.src = src;
  }

  public PirValue getOld() {
    return old;
  }

  public void setOld(PirValue old) {
    this.old = old;
  }

  public List<Integer> getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + old + index + src;
  }

}
