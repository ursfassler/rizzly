package pir.statement;

import pir.expression.PExpression;
import pir.other.Variable;
import evl.expression.RelOp;

public class Relation extends VariableGeneratorStmt {
  final private PExpression left; // FIXME only use constant or variable ref
  final private PExpression right; // FIXME only use constant or variable ref
  final private RelOp op;

  public Relation(Variable variable, PExpression left, PExpression right, RelOp op) {
    super(variable);
    this.left = left;
    this.right = right;
    this.op = op;
  }

  public PExpression getLeft() {
    return left;
  }

  public PExpression getRight() {
    return right;
  }

  public RelOp getOp() {
    return op;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + left + " " + op + " " + right;
  }

}
