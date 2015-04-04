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
import java.util.List;
import java.util.Set;

import util.Range;

import common.ElementInfo;

import evl.copy.Copy;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

public class KnowBaseItem extends KnowledgeEntry {

  private KnowledgeBase kb;
  private KnowUniqueName kun;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
    kun = kb.getEntry(KnowUniqueName.class);
  }

  public <T extends Evl> List<T> findItem(Class<T> kind) {
    Namespace r = kb.getRoot();
    return ClassGetter.filter(kind, r.children);
  }

  public Evl findItem(String name) {
    return kb.getRoot().children.find(name);
  }

  @Deprecated
  public void addItem(Evl item) {
    kb.getRoot().children.add(item);
  }

  private <T extends Type> T getPlainType(T type) {
    List<? extends Type> types = findItem(type.getClass());
    if (types.isEmpty()) {
      addItem(type);
      return type;
    } else {
      assert (types.size() == 1);
      return (T) types.get(0);
    }
  }

  public VoidType getVoidType() {
    return getPlainType(new VoidType());
  }

  /**
   * Returns R{0,count-1}
   *
   * @param count
   * @return
   */
  public RangeType getRangeType(int count) {
    BigInteger low = BigInteger.ZERO;
    BigInteger high = BigInteger.valueOf(count - 1);
    return getRangeType(new Range(low, high));
  }

  public RangeType getRangeType(Range range) {
    EvlList<RangeType> items = kb.getRoot().children.getItems(RangeType.class);
    for (RangeType itr : items) {
      if (itr.range.equals(range)) {
        return itr;
      }
    }
    RangeType ret = new RangeType(range);
    kb.getRoot().children.add(ret);
    return ret;
  }

  public ArrayType getArray(BigInteger size, Type type) {
    EvlList<ArrayType> items = kb.getRoot().children.getItems(ArrayType.class);
    for (ArrayType itr : items) {
      if (itr.size.equals(size) && itr.type.link.equals(type)) {
        return itr;
      }
    }

    ArrayType ret = new ArrayType(size, new SimpleRef<Type>(ElementInfo.NO, type));
    kb.getRoot().children.add(ret);
    return ret;
  }

  public RecordType getRecord(EvlList<NamedElement> element) {
    EvlList<RecordType> items = kb.getRoot().children.getItems(RecordType.class);
    for (RecordType itr : items) {
      if (equal(element, itr.element)) {
        return itr;
      }
    }

    RecordType ret = new RecordType(ElementInfo.NO, kun.get("record"), Copy.copy(element));
    kb.getRoot().children.add(ret);
    return ret;
  }

  private boolean equal(EvlList<NamedElement> left, EvlList<NamedElement> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      if (!left.get(i).name.equals(right.get(i).name) || !left.get(i).ref.link.equals(right.get(i).ref.link)) {
        return false;
      }
    }
    return true;
  }

  public EnumType getEnumType(Set<String> elements) {
    EvlList<EnumType> items = kb.getRoot().children.getItems(EnumType.class);
    for (EnumType itr : items) {
      if (itr.getNames().equals(elements)) {
        return itr;
      }
    }

    EnumType ret = new EnumType(ElementInfo.NO, kun.get("enum"));
    for (String name : elements) {
      ret.getElement().add(new EnumElement(ElementInfo.NO, name));
    }
    kb.getRoot().children.add(ret);
    return ret;
  }

  public StringType getStringType() {
    return getPlainType(new StringType());
  }

  public AnyType getAnyType() {
    return getPlainType(new AnyType());
  }

  public BooleanType getBooleanType() {
    return getPlainType(new BooleanType());
  }

  public IntegerType getIntegerType() {
    return getPlainType(new IntegerType());
  }

  public NaturalType getNaturalType() {
    return getPlainType(new NaturalType());
  }

  public Type getType(Type ct) {
    KnowBaseItemTypeFinder finder = new KnowBaseItemTypeFinder();
    return finder.traverse(ct, this);
  }

}

class KnowBaseItemTypeFinder extends NullTraverser<Type, KnowBaseItem> {

  @Override
  protected Type visitDefault(Evl obj, KnowBaseItem param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitEnumType(EnumType obj, KnowBaseItem param) {
    return param.getEnumType(obj.getNames());
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, KnowBaseItem param) {
    return param.getBooleanType();
  }

  @Override
  protected Type visitRecordType(RecordType obj, KnowBaseItem param) {
    return param.getRecord(obj.element);
  }

  @Override
  protected Type visitArrayType(ArrayType obj, KnowBaseItem param) {
    return param.getArray(obj.size, obj.type.link);
  }

  @Override
  protected Type visitRangeType(RangeType obj, KnowBaseItem param) {
    return param.getRangeType(obj.range);
  }

}
