package pir.statement.normal.binop;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.normal.StmtSignes;

/**
 *
 * @author urs
 */
abstract public class Relation extends BinaryOp {
  private StmtSignes signes = StmtSignes.unknown;

  public Relation(SsaVariable variable, PirValue left, PirValue right) {
    super(variable, left, right);
  }
  
  public StmtSignes getSignes() {
    return signes;
  }

  public void setSignes(StmtSignes signes) {
    this.signes = signes;
  }

  @Override
  public String toString() {
    return super.toString() + " := " + signes + " " + getLeft().toString() + " " + getOpName() + " " + getRight().toString();
  }
}
