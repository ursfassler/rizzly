package pir.expression;

public class ArithmeticOp extends PExpression {
  final private PExpression left;
  final private PExpression right;
  final private ArOp op;

  public ArithmeticOp(PExpression left, PExpression right, ArOp op) {
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

  public ArOp getOp() {
    return op;
  }

  @Override
  public String toString() {
    return op.toString();
  }
  
  

}
