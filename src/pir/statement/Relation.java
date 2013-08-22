package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;
import evl.expression.RelOp;

public class Relation extends VariableGeneratorStmt {
  private PirValue left;
  private PirValue right;
  private RelOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public Relation(SsaVariable variable, PirValue left, PirValue right, RelOp op) {
    super(variable);
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public PirValue getLeft() {
    return left;
  }

  public PirValue getRight() {
    return right;
  }

  public RelOp getOp() {
    return op;
  }

  public StmtSignes getSignes() {
    return signes;
  }

  public void setSignes(StmtSignes signes) {
    this.signes = signes;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + signes + " " + left + " " + op + " " + right;
  }

}
