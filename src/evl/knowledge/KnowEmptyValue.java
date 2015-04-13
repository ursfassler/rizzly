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

package evl.knowledge;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

import evl.copy.Copy;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.expression.BoolValue;
import evl.data.expression.Expression;
import evl.data.expression.NamedElementsValue;
import evl.data.expression.NamedValue;
import evl.data.expression.Number;
import evl.data.expression.StringValue;
import evl.data.expression.TupleValue;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.traverser.NullTraverser;

public class KnowEmptyValue extends KnowledgeEntry {
  final private Map<Type, Expression> cache = new HashMap<Type, Expression>();
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Expression get(Type type) {
    if (!cache.containsKey(type)) {
      KnowEmptyValueGenerator generator = new KnowEmptyValueGenerator(kb);
      cache.put(type, generator.traverse(type, null));
    }
    return cache.get(type);
  }

}

class KnowEmptyValueGenerator extends NullTraverser<Expression, Void> {
  final private KnowType kt;

  public KnowEmptyValueGenerator(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected evl.data.expression.Expression visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected evl.data.expression.Expression visitBooleanType(BooleanType obj, Void param) {
    return new BoolValue(ElementInfo.NO, false);
  }

  @Override
  protected evl.data.expression.Expression visitStringType(StringType obj, Void param) {
    return new StringValue(ElementInfo.NO, "");
  }

  @Override
  protected evl.data.expression.Expression visitEnumType(EnumType obj, Void param) {
    evl.data.expression.reference.Reference ref = new Reference(ElementInfo.NO, obj);
    ref.offset.add(new RefName(ElementInfo.NO, obj.getElement().get(0).name));
    return ref;
  }

  @Override
  protected evl.data.expression.Expression visitIntegerType(IntegerType obj, Void param) {
    return new Number(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected evl.data.expression.Expression visitNaturalType(NaturalType obj, Void param) {
    return new Number(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected evl.data.expression.Expression visitRangeType(RangeType obj, Void param) {
    BigInteger val;
    if (obj.range.high.compareTo(BigInteger.ZERO) < 0) {
      val = obj.range.high;
    } else if (obj.range.low.compareTo(BigInteger.ZERO) > 0) {
      val = obj.range.low;
    } else {
      val = BigInteger.ZERO;
    }
    return new Number(ElementInfo.NO, val);
  }

  @Override
  protected evl.data.expression.Expression visitArrayType(ArrayType obj, Void param) {
    EvlList<Expression> tv = new EvlList<Expression>();
    Expression itm = visit(kt.get(obj.type), param);
    for (int i = 0; i < obj.size.intValue(); i++) {
      tv.add(Copy.copy(itm));
    }
    return new TupleValue(ElementInfo.NO, tv);
  }

  @Override
  protected evl.data.expression.Expression visitRecordType(RecordType obj, Void param) {
    EvlList<NamedValue> value = new EvlList<NamedValue>();

    for (NamedElement elem : obj.element) {
      value.add(new NamedValue(ElementInfo.NO, elem.name, visit(kt.get(elem.typeref), param)));
    }

    return new NamedElementsValue(ElementInfo.NO, value);
  }

}
