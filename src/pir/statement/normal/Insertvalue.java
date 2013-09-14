package pir.statement.normal;

import java.util.List;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class Insertvalue extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable dst;
  private PirValue old;
  private PirValue src;
  final private List<Integer> index;

  public Insertvalue(SsaVariable dst, PirValue old, PirValue src, List<Integer> index) {
    this.dst = dst;
    this.old = old;
    this.src = src;
    this.index = index;
  }

  @Override
  public SsaVariable getVariable() {
    return dst;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.dst = variable;
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
    return dst + " := " + old + index + src;
  }

}
