package pir.statement.normal;

import pir.other.PirValue;
import pir.other.SsaVariable;
import evl.expression.RelOp;

public class Relation extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;
  private PirValue left;
  private PirValue right;
  private RelOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public Relation(SsaVariable variable, PirValue left, PirValue right, RelOp op) {
    this.variable = variable;
    this.left = left;
    this.right = right;
    this.op = op;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
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

  public void setLeft(PirValue left) {
    this.left = left;
  }

  public void setRight(PirValue right) {
    this.right = right;
  }

  public void setOp(RelOp op) {
    this.op = op;
  }

  public StmtSignes getSignes() {
    return signes;
  }

  public void setSignes(StmtSignes signes) {
    this.signes = signes;
  }

  @Override
  public String toString() {
    return variable + " := " + signes + " " + left + " " + op + " " + right;
  }

}
