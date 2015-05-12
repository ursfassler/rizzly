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

package ast.pass.specializer;

import ast.ElementInfo;
import ast.data.Ast;
import ast.data.expression.Expression;
import ast.data.expression.value.AnyValue;
import ast.data.type.Type;
import ast.data.type.base.RangeType;
import ast.dispatcher.NullDispatcher;
import error.ErrorType;
import error.RError;

public class CheckTypeCast extends NullDispatcher<Expression, Expression> {
  static final private CheckTypeCast INSTANCE = new CheckTypeCast();

  public static ast.data.expression.Expression check(Type type, Expression value) {
    return INSTANCE.traverse(type, value);
  }

  @Override
  protected Expression visitDefault(Ast obj, Expression param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Expression visitRangeType(RangeType obj, Expression param) {
    ast.data.expression.value.NumberValue num = (ast.data.expression.value.NumberValue) param;
    if ((obj.range.low.compareTo(num.value) > 0) || (obj.range.high.compareTo(num.value) < 0)) {
      RError.err(ErrorType.Error, param.getInfo(), "value " + num.toString() + " not in range " + obj.toString());
      return new AnyValue(ElementInfo.NO);
    }
    return num;
  }

}
