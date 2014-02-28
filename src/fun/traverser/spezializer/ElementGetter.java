package fun.traverser.spezializer;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.traverser.typecheck.specific.ExpressionTypeChecker;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.ArrayValue;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.reference.RefIndex;
import fun.expression.reference.RefItem;

public class ElementGetter extends NullTraverser<Expression, Expression> {
  public static final ElementGetter INSTANCE = new ElementGetter();

  static public Expression get(Expression base, List<RefItem> offset) {
    for (RefItem itr : offset) {
      base = INSTANCE.visit(itr, base);
    }
    return base;
  }

  @Override
  protected Expression visitDefault(Fun obj, Expression param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Expression param) {
    return visit(obj.getIndex(), param);
  }

  @Override
  protected Expression visitNumber(Number obj, Expression param) {
    int idx = ExpressionTypeChecker.getAsInt(obj.getValue(), obj.toString());
    if (!(param instanceof ArrayValue)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Expected array value");
    }
    ArrayValue arrv = (ArrayValue) param;
    return arrv.getValue().get(idx);
  }

}
