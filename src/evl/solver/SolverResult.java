package evl.solver;

import util.Unsure;
import evl.expression.Expression;

public class SolverResult {
  final public Unsure result;
  final public Expression rhs;

  public SolverResult(Unsure result, Expression rhs) {
    super();
    this.result = result;
    this.rhs = rhs;
  }

}
