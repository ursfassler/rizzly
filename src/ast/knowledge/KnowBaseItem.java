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
import java.util.List;
import java.util.Set;

import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.Range;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.base.StringType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.VoidType;
import ast.data.type.template.TypeType;
import ast.specification.TypeFilter;
import ast.traverser.NullTraverser;

public class KnowBaseItem extends KnowledgeEntry {

  private KnowledgeBase kb;
  private KnowUniqueName kun;

  @Override
  public void init(KnowledgeBase kb) {
    this.kb = kb;
    kun = kb.getEntry(KnowUniqueName.class);
  }

  public <T extends Ast> List<T> findItem(Class<T> kind) {
    Namespace r = kb.getRoot();
    return TypeFilter.select(r.children, kind);
  }

  public Ast findItem(String name) {
    return kb.getRoot().children.find(name);
  }

  @Deprecated
  public void addItem(Ast item) {
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
    AstList<RangeType> items = TypeFilter.select(kb.getRoot().children, RangeType.class);
    for (RangeType itr : items) {
      if (itr.range.equals(range)) {
        return itr;
      }
    }
    RangeType ret = new RangeType(range);
    kb.getRoot().children.add(ret);
    return ret;
  }

  public TypeType getTypeType(Type type) {
    AstList<TypeType> items = TypeFilter.select(kb.getRoot().children, TypeType.class);
    for (TypeType itr : items) {
      assert (itr.getType().offset.isEmpty());
      if (itr.getType().link == type) {
        return itr;
      }
    }
    TypeType ret = new TypeType(ElementInfo.NO, new Reference(ElementInfo.NO, type));
    kb.getRoot().children.add(ret);
    return ret;
  }

  public ArrayType getArray(BigInteger size, Type type) {
    AstList<ArrayType> items = TypeFilter.select(kb.getRoot().children, ArrayType.class);
    for (ArrayType itr : items) {
      if (itr.size.equals(size) && ((SimpleRef<Type>) itr.type).link.equals(type)) {
        return itr;
      }
    }

    ArrayType ret = new ArrayType(size, new SimpleRef<Type>(ElementInfo.NO, type));
    kb.getRoot().children.add(ret);
    return ret;
  }

  public RecordType getRecord(AstList<NamedElement> element) {
    AstList<RecordType> items = TypeFilter.select(kb.getRoot().children, RecordType.class);
    for (RecordType itr : items) {
      if (equal(element, itr.element)) {
        return itr;
      }
    }

    RecordType ret = new RecordType(ElementInfo.NO, kun.get("record"), Copy.copy(element));
    kb.getRoot().children.add(ret);
    return ret;
  }

  public TupleType getTupleType(AstList<TypeRef> types) {
    AstList<TupleType> items = TypeFilter.select(kb.getRoot().children, TupleType.class);
    for (TupleType itr : items) {
      if (equalTypes(types, itr.types)) {
        return itr;
      }
    }

    TupleType ret = new TupleType(ElementInfo.NO, kun.get("tupleType"), Copy.copy(types));
    kb.getRoot().children.add(ret);
    return ret;
  }

  private boolean equalTypes(AstList<TypeRef> left, AstList<TypeRef> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      TypeRef leftItr = left.get(i);
      TypeRef rightItr = right.get(i);
      if (!equal(leftItr, rightItr)) {
        return false;
      }
    }
    return true;
  }

  private boolean equal(TypeRef left, TypeRef right) {
    // FIXME only works for simple ref
    return ((SimpleRef<Type>) left).link.equals(((SimpleRef<Type>) right).link);
  }

  private boolean equal(AstList<NamedElement> left, AstList<NamedElement> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      NamedElement leftItr = left.get(i);
      NamedElement rightItr = right.get(i);
      if (!equal(leftItr, rightItr)) {
        return false;
      }
    }
    return true;
  }

  private boolean equal(NamedElement left, NamedElement right) {
    // FIXME only works for simple ref
    return ((SimpleRef<Type>) left.typeref).link.equals(((SimpleRef<Type>) right.typeref).link) && left.name.equals(right.name);
  }

  public EnumType getEnumType(Set<String> elements) {
    AstList<EnumType> items = TypeFilter.select(kb.getRoot().children, EnumType.class);
    for (EnumType itr : items) {
      if (itr.getNames().equals(elements)) {
        return itr;
      }
    }

    EnumType ret = new EnumType(ElementInfo.NO, kun.get("enum"));
    for (String name : elements) {
      ret.element.add(new EnumElement(ElementInfo.NO, name));
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
    KnowBaseItemTypeFinder finder = new KnowBaseItemTypeFinder(kb);
    return finder.traverse(ct, this);
  }

}

class KnowBaseItemTypeFinder extends NullTraverser<Type, KnowBaseItem> {
  final private KnowType kt;

  public KnowBaseItemTypeFinder(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  @Override
  protected Type visitDefault(Ast obj, KnowBaseItem param) {
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
    return param.getArray(obj.size, kt.get(obj.type));
  }

  @Override
  protected Type visitRangeType(RangeType obj, KnowBaseItem param) {
    return param.getRangeType(obj.range);
  }

  @Override
  protected Type visitTupleType(TupleType obj, KnowBaseItem param) {
    return param.getTupleType(obj.types);
  }

}
