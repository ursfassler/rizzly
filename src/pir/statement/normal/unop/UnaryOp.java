package pir.statement.normal.unop;

import pir.other.PirValue;
import pir.other.SsaVariable;
import pir.statement.normal.NormalStmt;
import pir.statement.normal.StmtSignes;
import pir.statement.normal.VariableGeneratorStmt;

abstract public class UnaryOp extends NormalStmt implements VariableGeneratorStmt {

  private SsaVariable variable;
  private PirValue expr;
  private StmtSignes signes = StmtSignes.unknown;

  public UnaryOp(SsaVariable variable, PirValue expr) {
    this.variable = variable;
    this.expr = expr;
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

  public PirValue getExpr() {
    return expr;
  }

  public void setExpr(PirValue expr) {
    this.expr = expr;
  }

  public StmtSignes getSignes() {
    return signes;
  }

  public void setSignes(StmtSignes signes) {
    this.signes = signes;
  }

  @Override
  public String toString() {
    return variable + " := " + signes + " " + getOpName() + " " + expr.toString();
  }
}
