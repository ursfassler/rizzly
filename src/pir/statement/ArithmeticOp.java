package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class ArithmeticOp extends VariableGeneratorStmt {
  private PirValue left; // FIXME only use constant or variable ref
  private PirValue right;// FIXME only use constant or variable ref
  final private ArOp op;

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

  @Override
  public String toString() {
    return super.toString() + " := " + left.toString() + " " + op.toString() + " " + right.toString();
  }

}
