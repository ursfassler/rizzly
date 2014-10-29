package fun.traverser;

import java.math.BigInteger;

import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArrayValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.other.FunList;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.template.Array;

/**
 * Creates a (initialization) value from a type;
 */
public class ValueCreator extends NullTraverser<Expression, Void> {
  final static public ValueCreator INSTANCE = new ValueCreator();

  @Override
  protected Expression visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitIntegerType(IntegerType obj, Void param) {
    return new Number(obj.getInfo(), BigInteger.ZERO);
  }

  @Override
  protected Expression visitNaturalType(NaturalType obj, Void param) {
    return new Number(obj.getInfo(), BigInteger.ZERO);
  }

  @Override
  protected Expression visitArray(Array obj, Void param) {
    int size = ExpressionTypeChecker.getAsInt(obj.getSize(), obj.toString());
    FunList<Expression> values = new FunList<Expression>();
    for (int i = 0; i < size; i++) {
      values.add(visit(obj.getType().getLink(), null));
    }
    return new ArrayValue(obj.getInfo(), values);
  }

}
