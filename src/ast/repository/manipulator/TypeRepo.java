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

package ast.repository.manipulator;

import java.math.BigInteger;
import java.util.Set;

import ast.ElementInfo;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Range;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.TypeRef;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.base.ArrayTypeFactory;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.EnumTypeFactory;
import ast.data.type.base.RangeType;
import ast.data.type.base.RangeTypeFactory;
import ast.data.type.base.StringType;
import ast.data.type.base.TupleType;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.special.AnyType;
import ast.data.type.special.IntegerType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.VoidType;
import ast.data.type.template.TypeType;
import ast.knowledge.KnowUniqueName;
import ast.knowledge.KnowledgeBase;

public class TypeRepo {
  final private RepoAdder ra;
  final private KnowUniqueName kun;

  public TypeRepo(KnowledgeBase kb) {
    ra = new RepoAdder(kb);
    kun = kb.getEntry(KnowUniqueName.class);
  }

  public EnumType getEnumType(Set<String> elements) {
    return getType(EnumTypeFactory.create(kun.get("enum"), elements));
  }

  public TupleType getTupleType(AstList<TypeRef> types) {
    return getType(new TupleType(ElementInfo.NO, kun.get("tupleType"), Copy.copy(types)));
  }

  public TypeType getTypeType(Type type) {
    return getType(new TypeType(ElementInfo.NO, new Reference(ElementInfo.NO, type)));
  }

  public RecordType getRecord(AstList<NamedElement> element) {
    return getType(new RecordType(ElementInfo.NO, kun.get("record"), Copy.copy(element)));
  }

  public ArrayType getArray(BigInteger size, Type type) {
    return getType(ArrayTypeFactory.create(size, type));
  }

  public VoidType getVoidType() {
    return getType(new VoidType());
  }

  public RangeType getRangeType(int count) {
    return getType(RangeTypeFactory.create(count));
  }

  public RangeType getRangeType(Range range) {
    return getType(RangeTypeFactory.create(range));
  }

  public StringType getStringType() {
    return getType(new StringType());
  }

  public AnyType getAnyType() {
    return getType(new AnyType());
  }

  public BooleanType getBooleanType() {
    return getType(new BooleanType());
  }

  public IntegerType getIntegerType() {
    return getType(new IntegerType());
  }

  public NaturalType getNaturalType() {
    return getType(new NaturalType());
  }

  public <T extends Type> T getType(T ct) {
    return ra.replaceOrRegister(ct);
  }

}
