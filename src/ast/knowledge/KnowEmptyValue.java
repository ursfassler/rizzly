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

package ast.knowledge;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.ValueExpr;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.dispatcher.NullDispatcher;

public class KnowEmptyValue extends KnowledgeEntry {
  final private Map<Type, ValueExpr> cache = new HashMap<Type, ValueExpr>();
  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public ValueExpr get(Type type) {
    if (!cache.containsKey(type)) {
      KnowEmptyValueGenerator generator = new KnowEmptyValueGenerator(kb);
      cache.put(type, generator.traverse(type, null));
    }
    return cache.get(type);
  }

}

class KnowEmptyValueGenerator extends NullDispatcher<ValueExpr, Void> {
  final private KnowType kt;

  public KnowEmptyValueGenerator(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected ValueExpr visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected ValueExpr visitBooleanType(BooleanType obj, Void param) {
    return new BoolValue(ElementInfo.NO, false);
  }

  @Override
  protected ValueExpr visitStringType(StringType obj, Void param) {
    return new StringValue(ElementInfo.NO, "");
  }

  @Override
  protected ValueExpr visitEnumType(EnumType obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
    // Reference ref = RefFactory.create(ElementInfo.NO, obj, new RefName(ElementInfo.NO, obj.element.get(0).name));
    // return new RefExp(ElementInfo.NO, ref);
  }

  @Override
  protected ValueExpr visitIntegerType(IntegerType obj, Void param) {
    return new NumberValue(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected ValueExpr visitNaturalType(NaturalType obj, Void param) {
    return new NumberValue(ElementInfo.NO, BigInteger.ZERO);
  }

  @Override
  protected ValueExpr visitRangeType(RangeType obj, Void param) {
    BigInteger val;
    if (obj.range.high.compareTo(BigInteger.ZERO) < 0) {
      val = obj.range.high;
    } else if (obj.range.low.compareTo(BigInteger.ZERO) > 0) {
      val = obj.range.low;
    } else {
      val = BigInteger.ZERO;
    }
    return new NumberValue(ElementInfo.NO, val);
  }

  @Override
  protected ValueExpr visitArrayType(ArrayType obj, Void param) {
    AstList<Expression> tv = new AstList<Expression>();
    Expression itm = visit(kt.get(obj.type), param);
    for (int i = 0; i < obj.size.intValue(); i++) {
      tv.add(Copy.copy(itm));
    }
    return new TupleValue(ElementInfo.NO, tv);
  }

  @Override
  protected ValueExpr visitRecordType(RecordType obj, Void param) {
    AstList<NamedValue> value = new AstList<NamedValue>();

    for (NamedElement elem : obj.element) {
      value.add(new NamedValue(ElementInfo.NO, elem.name, visit(kt.get(elem.typeref), param)));
    }

    return new NamedElementsValue(ElementInfo.NO, value);
  }

}
