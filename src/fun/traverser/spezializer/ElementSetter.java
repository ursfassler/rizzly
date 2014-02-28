package fun.traverser.spezializer;

import util.Pair;
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

public class ElementSetter extends NullTraverser<Expression, Pair<Expression, Expression>> {
  private static final ElementSetter INSTANCE = new ElementSetter();

  static public void set(Expression parent, RefItem offset, Expression value) {
    INSTANCE.visit(offset, new Pair<Expression, Expression>(parent, value));
  }

  @Override
  protected Expression visitDefault(Fun obj, Pair<Expression, Expression> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitRefIndex(RefIndex obj, Pair<Expression, Expression> param) {
    visit(obj.getIndex(), param);
    return null;
  }

  @Override
  protected Expression visitNumber(Number obj, Pair<Expression, Expression> param) {
    int idx = ExpressionTypeChecker.getAsInt(obj.getValue(), obj.toString());
    if (!(param.first instanceof ArrayValue)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Expected array value");
    }
    ArrayValue arrv = (ArrayValue) param.first;
    arrv.getValue().set(idx, param.second);
    return null;
  }

}
