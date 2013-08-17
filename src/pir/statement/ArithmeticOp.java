package pir.statement;

import pir.expression.PExpression;
import pir.other.Variable;

public class ArithmeticOp extends VariableGeneratorStmt {
  final private PExpression left; // FIXME only use constant or variable ref
  final private PExpression right;// FIXME only use constant or variable ref
  final private ArOp op;

  public ArithmeticOp(Variable variable, PExpression left, PExpression right, ArOp op) {
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

  public ArOp getOp() {
    return op;
  }

  @Override
  public String toString() {
    return super.toString() + " := " + left.toString() + " " + op.toString() + " " + right.toString();
  }

}
