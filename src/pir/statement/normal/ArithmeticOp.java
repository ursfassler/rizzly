package pir.statement.normal;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class ArithmeticOp extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;
  private PirValue left;
  private PirValue right;
  private ArOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public ArithmeticOp(SsaVariable variable, PirValue left, PirValue right, ArOp op) {
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

  public void setLeft(PirValue left) {
    this.left = left;
  }

  public void setRight(PirValue right) {
    this.right = right;
  }

  public ArOp getOp() {
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
    return super.toString() + " := " + signes + " " + left.toString() + " " + op.toString() + " " + right.toString();
  }

}
