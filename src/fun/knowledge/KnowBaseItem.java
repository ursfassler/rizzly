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

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.reference.Reference;
import fun.expression.reference.SimpleRef;
import fun.other.FunList;
import fun.other.Named;
import fun.type.Type;
import fun.type.base.BooleanType;
import fun.type.base.IntegerType;
import fun.type.base.NaturalType;
import fun.type.base.StringType;
import fun.type.base.VoidType;
import fun.type.template.Array;
import fun.type.template.Range;
import fun.type.template.TypeType;

public class KnowBaseItem extends KnowledgeEntry {

  private KnowledgeBase kb;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
  }

  public Named findItem(String name) {
    return kb.getRoot().getChildren().find(name);
  }

  public <T extends Type> T findItem(T type) {
    FunList<? extends Type> items = kb.getRoot().getChildren().getItems(type.getClass());
    assert (items.size() <= 1);
    return (T) (items.isEmpty() ? null : items.get(0));
  }

  public void addItem(Type item) {
    assert (findItem(item) == null);
    kb.getRoot().getChildren().add(item);
  }

  public void addItem(Named item) {
    assert (findItem(item.getName()) == null);
    kb.getRoot().getChildren().add(item);
  }

  // --------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public <T extends Named> T get(Class<T> kind, String name) {
    Named item = findItem(name);
    if (item == null) {
      RError.err(ErrorType.Fatal, "Base item not found: " + name);
      return null;
    }
    if (!kind.isAssignableFrom(item.getClass())) {
      RError.err(ErrorType.Fatal, "Base item is of wrong type. Expected: " + kind.getCanonicalName() + "; got: " + item.getClass().getCanonicalName());
      return null;
    }
    return (T) item;
  }

  private <T extends Type> T getPlainType(T type) {
    T ret = findItem(type);
    if (ret == null) {
      ret = type;
      addItem(ret);
    }
    return ret;
  }

  public Range getRangeType(BigInteger low, BigInteger high) {
    FunList<Range> items = kb.getRoot().getChildren().getItems(Range.class);
    for (Range itr : items) {
      if (itr.getLow().equals(low) && itr.getHigh().equals(high)) {
        return itr;
      }
    }
    Range ret = new Range(ElementInfo.NO, low, high);
    kb.getRoot().getChildren().add(ret);
    return ret;
  }

  public Array getArray(BigInteger size, Type type) {
    FunList<Array> items = kb.getRoot().getChildren().getItems(Array.class);
    for (Array itr : items) {
      if (itr.getSize().equals(size) && itr.getType().getLink() == type) {
        return itr;
      }
    }
    Array ret = new Array(ElementInfo.NO, size, new SimpleRef(ElementInfo.NO, type));
    kb.getRoot().getChildren().add(ret);
    return ret;
  }

  public TypeType getTypeType(Type type) {
    FunList<TypeType> items = kb.getRoot().getChildren().getItems(TypeType.class);
    for (TypeType itr : items) {
      assert (itr.getType().getOffset().isEmpty());
      if (itr.getType().getLink() == type) {
        return itr;
      }
    }
    TypeType ret = new TypeType(ElementInfo.NO, new Reference(ElementInfo.NO, type));
    kb.getRoot().getChildren().add(ret);
    return ret;
  }

  public VoidType getVoidType() {
    return getPlainType(VoidType.INSTANCE);
  }

  public StringType getStringType() {
    return getPlainType(new StringType());
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

}
