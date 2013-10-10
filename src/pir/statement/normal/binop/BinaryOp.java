package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.normal.NormalStmt;
import pir.statement.normal.StmtSignes;
import pir.statement.normal.VariableGeneratorStmt;

abstract public class BinaryOp extends NormalStmt implements VariableGeneratorStmt {

  private SsaVariable variable;
  private PirValue left;
  private PirValue right;

  public BinaryOp(SsaVariable variable, PirValue left, PirValue right) {
    this.variable = variable;
    this.left = left;
    this.right = right;
  }

  abstract public String getOpName();

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

}
