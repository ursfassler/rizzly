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

package fun.knowledge;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import fun.Copy;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.AnyValue;
import fun.expression.Expression;
import fun.expression.TupleValue;
import fun.other.FunList;
import fun.type.Type;
import fun.type.base.BooleanType;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.template.Array;
import fun.type.template.Range;

public class KnowEmptyValue extends KnowledgeEntry {
  final private Map<Type, Expression> cache = new HashMap<Type, Expression>();

  @Override
  public void init(KnowledgeBase base) {
  }

  public Expression get(Type type) {
    if (!cache.containsKey(type)) {
      KnowEmptyValueGenerator generator = new KnowEmptyValueGenerator();
      cache.put(type, generator.traverse(type, null));
    }
    return cache.get(type);
  }

}

class KnowEmptyValueGenerator extends NullTraverser<Expression, Void> {

  @Override
  protected Expression visitDefault(Fun obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Expression visitBooleanType(BooleanType obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitStringType(StringType obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitEnumType(EnumType obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitIntegerType(IntegerType obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitNaturalType(NaturalType obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitRange(Range obj, Void param) {
    return new AnyValue(ElementInfo.NO);
  }

  @Override
  protected Expression visitArray(Array obj, Void param) {
    FunList<Expression> tv = new FunList<Expression>();
    Expression itm = visit(obj.getType().getLink(), param);
    for (int i = 0; i < obj.getSize().intValue(); i++) {
      tv.add(Copy.copy(itm));
    }
    return new TupleValue(ElementInfo.NO, tv);
  }
}
