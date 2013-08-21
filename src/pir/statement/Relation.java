package pir.statement;

import pir.other.PirValue;
import pir.other.SsaVariable;
import evl.expression.RelOp;

public class Relation extends VariableGeneratorStmt {
  final private PirValue left; // FIXME only use constant or variable ref
  final private PirValue right; // FIXME only use constant or variable ref
  final private RelOp op;

  public Relation(SsaVariable variable, PirValue left, PirValue right, RelOp op) {
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

  public RelOp getOp() {
    return op;
  }

  @Override
  public String toString() {
    return getVariable() + " := " + left + " " + op + " " + right;
  }

}
