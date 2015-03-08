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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import fun.Copy;
import fun.Fun;
import fun.NullTraverser;
import fun.expression.BoolValue;
import fun.expression.Expression;
import fun.expression.NamedElementsValue;
import fun.expression.NamedValue;
import fun.expression.Number;
import fun.expression.StringValue;
import fun.expression.TupleValue;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.type.Type;
import fun.type.base.BooleanType;
import fun.type.base.EnumType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.composed.NamedElement;
import fun.type.composed.RecordType;
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
    return new BoolValue(ElementInfo.NO, false);
  }

  @Override
  protected Expression visitStringType(StringType obj, Void param) {
    return new StringValue(ElementInfo.NO, "");
  }

  @Override
  protected Expression visitEnumType(EnumType obj, Void param) {
    Reference ref = new Reference(ElementInfo.NO, obj);
    ref.getOffset().add(new RefName(ElementInfo.NO, obj.getElement().get(0).getName()));
    return ref;
  }

  @Override
  protected Expression visitIntegerType(IntegerType obj, Void param) {
    return new fun.expression.Number(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected Expression visitNaturalType(NaturalType obj, Void param) {
    return new fun.expression.Number(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected Expression visitRange(Range obj, Void param) {
    BigInteger val;
    if (obj.getHigh().compareTo(BigInteger.ZERO) < 0) {
      val = obj.getHigh();
    } else if (obj.getLow().compareTo(BigInteger.ZERO) > 0) {
      val = obj.getLow();
    } else {
      val = BigInteger.ZERO;
    }
    return new Number(ElementInfo.NO, val);
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

  @Override
  protected Expression visitRecordType(RecordType obj, Void param) {
    FunList<NamedValue> value = new FunList<NamedValue>();

    for (NamedElement elem : obj.getElement()) {
      value.add(new NamedValue(ElementInfo.NO, elem.getName(), visit(elem.getType().getLink(), param)));
    }

    return new NamedElementsValue(ElementInfo.NO, value);
  }

}
