package pir.traverser;

import pir.expression.ArrayValue;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.StringValue;
import pir.expression.UnaryExpr;
import pir.expression.reference.Reference;
import pir.other.Program;
import pir.statement.ArithmeticOp;
import pir.type.EnumElement;

public class EnumElementConstPropagation extends ExprReplacer<Void> {

  public static void process(Program prog) {
    EnumElementConstPropagation propagation = new EnumElementConstPropagation();
    propagation.traverse(prog, null);
  }

  @Override
  protected PExpression visitBoolValue(BoolValue obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitNumber(Number obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitStringValue(StringValue obj, Void param) {
    return obj;
  }

  @Override
  protected PExpression visitArrayValue(ArrayValue obj, Void param) {
    visitExprList(obj.getValue(), param);
    return obj;
  }

  @Override
  protected PExpression visitArithmeticOp(ArithmeticOp obj, Void param) {
    PExpression le = visit(obj.getLeft(), param);
    PExpression re = visit(obj.getRight(), param);
    return new ArithmeticOp(le, re, obj.getOp());
  }

  @Override
  protected PExpression visitUnaryExpr(UnaryExpr obj, Void param) {
    PExpression expr = visit(obj.getExpr(), param);
    return new UnaryExpr(obj.getOp(), expr);
  }

  @Override
  protected PExpression visitRelation(Relation obj, Void param) {
    PExpression le = visit(obj.getLeft(), param);
    PExpression re = visit(obj.getRight(), param);
    return new Relation(le, re, obj.getOp());
  }

  @Override
  protected PExpression visitReference(Reference obj, Void param) {
    PExpression ref = visit(obj.getRef(), param);
    if (ref != null) {
      return ref;
    } else {
      return obj;
    }
  }

  @Override
  protected PExpression visitRefHead(RefHead obj, Void param) {
    if (obj.getRef() instanceof EnumElement) {
      EnumElement elem = (EnumElement) obj.getRef();
      return new Number(elem.getIntValue());
    } else {
      return null;
    }
  }

}
