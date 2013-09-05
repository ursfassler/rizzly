package pir.know;

import pir.NullTraverser;
import pir.PirObject;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.function.Function;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.statement.Relation;
import pir.type.BooleanType;
import pir.type.Type;

public class IsBoolean extends NullTraverser<Boolean, Void> {

  public static boolean test(PExpression obj) {
    IsBoolean pirType = new IsBoolean();
    return pirType.traverse(obj, null);
  }

  private static boolean test(Type type) {
    return type instanceof BooleanType;
  }

  @Override
  protected Boolean doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visitFunction(Function obj, Void param) {
    return false; // its a function type
  }

  @Override
  protected Boolean visitArithmeticOp(ArithmeticOp obj, Void param) {
    boolean lhs = visit(obj.getLeft(), param);
    boolean rhs = visit(obj.getRight(), param);
    assert (lhs == rhs);
    return lhs;
  }

//  @Override
//  protected Boolean visitUnaryExpr(UnaryExpr obj, Void param) {
//    switch (obj.getOp()) {
//    case MINUS:
//      return false;
//    case NOT:
//      return true;
//    default:
//      throw new RuntimeException("not yet implemented: " + obj.getOp());
//    }
//  }

  @Override
  protected Boolean visitRelation(Relation obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitVariable(Variable obj, Void param) {
    return test(obj.getType().getRef());
  }

  @Override
  protected Boolean visitNumber(Number obj, Void param) {
    return false;
  }

}
