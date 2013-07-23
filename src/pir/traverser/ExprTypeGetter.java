package pir.traverser;

import pir.NullTraverser;
import pir.PirObject;
import pir.expression.ArithmeticOp;
import pir.expression.BoolValue;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.Reference;
import pir.expression.Relation;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefCall;
import pir.expression.reference.RefHead;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.other.Variable;
import pir.type.BooleanType;
import pir.type.EnumType;
import pir.type.Type;
import pir.type.UnsignedType;

public class ExprTypeGetter extends NullTraverser<Type, Void> {
  static public Type process(PExpression ast) {
    ExprTypeGetter adder = new ExprTypeGetter();
    return adder.traverse(ast, null);
  }

  @Override
  protected Type doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Void param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Type visitRefHead(RefHead obj, Void param) {
    return visit((PirObject) obj.getRef(), param);
  }

  @Override
  protected Type visitRefCall(RefCall obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitRefName(RefName obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Type visitUnaryExpr(UnaryExpr obj, Void param) {
    Type expt = visit(obj.getExpr(), param);
    return expt;
  }

  @Override
  protected Type visitRelation(Relation obj, Void sym) {
    return new BooleanType();
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, Void sym) {
    Type lhs = visit(obj.getLeft(), sym);
    Type rhs = visit(obj.getRight(), sym);
    Type bigger;
    // TODO implement correct test, may depend on operator
    if ((lhs instanceof EnumType) || (rhs instanceof EnumType)) {
      bigger = null;
    } else if (TypeContainer.leftIsContainer(lhs, rhs)) {
      bigger = lhs;
    } else if (TypeContainer.leftIsContainer(rhs, lhs)) {
      bigger = rhs;
    } else {
      bigger = null;
    }
    return bigger;
  }

  static private int getBits(int value) {
    int bit = 0;
    while (value != 0) {
      value >>= 1;
      bit++;
    }
    return bit;
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    if (obj.getValue() >= 0) {
      int bits = getBits(obj.getValue());
      return new UnsignedType(bits);
    } else {
      throw new RuntimeException("not yet implemented");
    }
  }

  @Override
  protected Type visitBoolValue(BoolValue obj, Void param) {
    return new BooleanType();
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return obj.getType();
  }

}
