package fun.traverser;

import java.util.HashMap;
import java.util.Map;

import fun.expression.Expression;

public class ElemReplacer extends ExprReplacer<Map<Expression, Expression>> {
  public final static ElemReplacer INSTANCE = new ElemReplacer();

  public static Expression replace(Expression base, Expression oldExpr, Expression newExpr) {
    HashMap<Expression, Expression> map = new HashMap<Expression, Expression>();
    map.put(oldExpr, newExpr);
    return INSTANCE.traverse(base, map);
  }

  @Override
  protected Expression visitExpression(Expression obj, Map<Expression, Expression> param) {
    if (param.containsKey(obj)) {
      obj = param.get(obj);
      assert (obj != null);
    }
    return super.visitExpression(obj, param);
  }

}
