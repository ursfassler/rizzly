package pir.statement;

import java.util.List;

import pir.other.Variable;

public class Insertvalue extends VariableGeneratorStmt {
  private Variable old;
  private Variable src;
  final private List<Integer> index;

  public Insertvalue(Variable dst, Variable old, Variable src, List<Integer> index) {
    super(dst);
    this.old = old;
    this.src = src;
    this.index = index;
  }

  public Variable getSrc() {
    return src;
  }

  public void setSrc(Variable src) {
    this.src = src;
  }

  public Variable getOld() {
    return old;
  }

  public void setOld(Variable old) {
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
