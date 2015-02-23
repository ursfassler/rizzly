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

import util.Pair;
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
  protected Expression visitRefCall(RefCall obj, Pair<Expression, Expression> param) {
    RError.ass(obj.getActualParameter().getValue().size() == 1, obj.getInfo());
    visit(obj.getActualParameter().getValue().get(0), param);
    return null;
  }

  @Override
  protected Expression visitNumber(Number obj, Pair<Expression, Expression> param) {
    int idx = ExpressionTypeChecker.getAsInt(obj.getValue(), obj.toString());
    if (!(param.first instanceof TupleValue)) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Expected array value");
    }
    TupleValue arrv = (TupleValue) param.first;
    arrv.getValue().set(idx, param.second);
    return null;
  }

}
