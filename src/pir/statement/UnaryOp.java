package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class UnaryOp extends VariableGeneratorStmt {
  private PirValue expr;
  private evl.expression.UnaryOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public UnaryOp(SsaVariable variable, PirValue expr, evl.expression.UnaryOp op) {
    super(variable);
    this.expr = expr;
    this.op = op;
  }

  public PirValue getExpr() {
    return expr;
  }

  public void setExpr(PirValue expr) {
    this.expr = expr;
  }

  public evl.expression.UnaryOp getOp() {
    return op;
  }

  public void setOp(evl.expression.UnaryOp op) {
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
    return super.toString() + " := " + signes + " " + op.toString() + " " + expr.toString();
  }

}
