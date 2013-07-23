package pir.expression;

public class Relation extends PExpression {
  final private PExpression left;
  final private PExpression right;
  final private RelOp op;

  public Relation(PExpression left, PExpression right, RelOp op) {
    super();
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
    return op.toString();
  }

}
