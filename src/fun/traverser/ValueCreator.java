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

package fun.traverser;

import java.math.BigInteger;

import evl.pass.check.type.specific.ExpressionTypeChecker;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.Expression;
import fun.expression.Number;
import fun.expression.TupleValue;
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
    return new TupleValue(obj.getInfo(), values);
  }

}
