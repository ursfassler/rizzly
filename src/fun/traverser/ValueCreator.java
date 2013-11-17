package fun.traverser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArrayValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.Reference;
import fun.type.Type;
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

  private Type getType(Reference ref) {
    assert (ref.getOffset().isEmpty());
    return (Type) ref.getLink();
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
    List<Expression> values = new ArrayList<Expression>(size);
    for (int i = 0; i < size; i++) {
      values.add(visit(getType(obj.getType()), null));
    }
    return new ArrayValue(obj.getInfo(), values);
  }

}
