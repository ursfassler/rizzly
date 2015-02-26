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
import fun.Fun;
import fun.expression.Expression;
import fun.traverser.ExprReplacer;

public class ValueReplacer extends ExprReplacer<Pair<Expression, Expression>> {
  static final private ValueReplacer INSTANCE = new ValueReplacer();

  static public Expression set(Expression root, Expression oldValue, Expression newValue) {
    return INSTANCE.traverse(root, new Pair<Expression, Expression>(oldValue, newValue));
  }

  @Override
  protected Expression visit(Fun obj, Pair<Expression, Expression> param) {
    if (obj == param.first) {
      return param.second;
    } else {
      return super.visit(obj, param);
    }
  }

}
