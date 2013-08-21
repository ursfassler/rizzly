package pir.know;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import pir.NullTraverser;
import pir.PirObject;
import pir.expression.Number;
import pir.expression.PExpression;
import pir.expression.UnOp;
import pir.expression.UnaryExpr;
import pir.expression.reference.RefIndex;
import pir.expression.reference.RefName;
import pir.function.Function;
import pir.other.FuncVariable;
import pir.other.Variable;
import pir.statement.ArithmeticOp;
import pir.traverser.TypeContainer;
import pir.type.ArrayType;
import pir.type.BooleanType;
import pir.type.FunctionType;
import pir.type.NamedElemType;
import pir.type.NamedElement;
import pir.type.RangeType;
import pir.type.Type;
import error.ErrorType;
import error.RError;

public class KnowPirType extends NullTraverser<Type, Void> {

  public static Type get(PExpression obj) {
    KnowPirType pirType = new KnowPirType();
    return pirType.traverse(obj, null);
  }

  @Override
  protected Type doDefault(PirObject obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitFunction(Function obj, Void param) {
    List<Type> arg = new ArrayList<Type>();
    for (FuncVariable var : obj.getArgument()) {
      arg.add(var.getType());
    }
    return new FunctionType(arg, obj.getRetType());
  }

  @Override
  protected Type visitArithmeticOp(ArithmeticOp obj, Void param) {
    Type lhs = visit(obj.getLeft(), param);
    Type rhs = visit(obj.getRight(), param);
    Type bigger = null;
    if (TypeContainer.leftIsContainer(lhs, rhs)) {
      bigger = lhs;
    } else if (TypeContainer.leftIsContainer(rhs, lhs)) {
      bigger = rhs;
    } else {
      RError.err(ErrorType.Fatal, "Incompatible types");
    }
    return bigger;
  }

  @Override
  protected Type visitUnaryExpr(UnaryExpr obj, Void param) {
    Type type = visit(obj.getExpr(), param);
    if (type instanceof BooleanType) {
      assert (obj.getOp() == UnOp.NOT);
      return type;
    } else if (type instanceof RangeType) {
      assert (obj.getOp() == UnOp.MINUS);
      BigInteger low = ((RangeType) type).getLow();
      BigInteger high = ((RangeType) type).getHigh();
      low = BigInteger.ZERO.subtract(low);
      high = BigInteger.ZERO.subtract(high);
      return new RangeType(high, low);
    } else {
      RError.err(ErrorType.Fatal, "Unsupported type: " + type.getClass().getCanonicalName() + " / " + type);
      return null;
    }
  }

  @Override
  protected Type visitVariable(Variable obj, Void param) {
    return obj.getType();
  }

  @Override
  protected Type visitNumber(Number obj, Void param) {
    BigInteger value = BigInteger.valueOf(obj.getValue());
    return new RangeType(value, value);
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Void param) {
    Type type = visit(obj.getPrevious(), param);
    assert (type instanceof ArrayType);
    ArrayType at = (ArrayType) type;
    return at.getType();
  }

  @Override
  protected Type visitRefName(RefName obj, Void param) {
    Type type = visit(obj.getPrevious(), param);
    assert (type instanceof NamedElemType);
    NamedElemType nt = (NamedElemType) type;
    NamedElement elem = nt.find(obj.getName());
    assert (elem != null);
    return elem.getType();
  }

}
