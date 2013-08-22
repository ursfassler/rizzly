package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class ArithmeticOp extends VariableGeneratorStmt {
  private PirValue left;
  private PirValue right;
  private ArOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public ArithmeticOp(SsaVariable variable, PirValue left, PirValue right, ArOp op) {
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
