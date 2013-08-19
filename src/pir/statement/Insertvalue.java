package pir.statement;

import java.util.List;

import pir.expression.reference.VarRef;
import pir.other.Variable;

public class Insertvalue extends VariableGeneratorStmt {
  private VarRef old;
  private VarRef src;
  final private List<Integer> index;

  public Insertvalue(Variable dst, VarRef old, VarRef src, List<Integer> index) {
    super(dst);
    this.old = old;
    this.src = src;
    this.index = index;
  }

  public VarRef getSrc() {
    return src;
  }

  public void setSrc(VarRef src) {
    this.src = src;
  }

  public VarRef getOld() {
    return old;
  }

  public void setOld(VarRef old) {
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
