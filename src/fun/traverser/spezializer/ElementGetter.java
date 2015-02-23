/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package fun.traverser.spezializer;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.pass.check.type.specific.ExpressionTypeChecker;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.TupleValue;
import fun.expression.reference.RefCall;
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
  protected Expression visitRefCall(RefCall obj, Expression param) {
    RError.ass(obj.getActualParameter().getValue().size() == 1, obj.getInfo());
    return visit(obj.getActualParameter().getValue().get(0), param);
  }

  @Override
  protected Expression visitNumber(Number obj, Expression param) {
    int idx = ExpressionTypeChecker.getAsInt(obj.getValue(), obj.toString());
    if (!(param instanceof TupleValue)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Expected array value");
    }
    TupleValue arrv = (TupleValue) param;
    return arrv.getValue().get(idx);
  }

}
