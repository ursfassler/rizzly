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

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.expression.AnyValue;
import evl.data.expression.Expression;
import evl.data.type.Type;
import evl.data.type.base.RangeType;
import evl.traverser.NullTraverser;

public class CheckTypeCast extends NullTraverser<Expression, Expression> {
  static final private CheckTypeCast INSTANCE = new CheckTypeCast();

  public static evl.data.expression.Expression check(Type type, Expression value) {
    return INSTANCE.traverse(type, value);
  }

  @Override
  protected evl.data.expression.Expression visitDefault(Evl obj, Expression param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected evl.data.expression.Expression visitRangeType(RangeType obj, Expression param) {
    evl.data.expression.Number num = (evl.data.expression.Number) param;
    if ((obj.range.low.compareTo(num.value) > 0) || (obj.range.high.compareTo(num.value) < 0)) {
      RError.err(ErrorType.Error, param.getInfo(), "value " + num.toString() + " not in range " + obj.toString());
      return new AnyValue(ElementInfo.NO);
    }
    return num;
  }

}
