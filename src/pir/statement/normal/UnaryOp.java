package pir.statement.normal;

import pir.other.PirValue;
import pir.other.SsaVariable;

public class UnaryOp extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;
  private PirValue expr;
  private evl.expression.UnaryOp op;
  private StmtSignes signes = StmtSignes.unknown;

  public UnaryOp(SsaVariable variable, PirValue expr, evl.expression.UnaryOp op) {
    this.variable = variable;
    this.expr = expr;
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
    return variable + " := " + signes + " " + op.toString() + " " + expr.toString();
  }

}
