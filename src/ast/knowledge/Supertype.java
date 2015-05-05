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

import ast.data.Ast;
import ast.data.type.Type;
import ast.data.type.base.BooleanType;
import ast.data.type.base.EnumType;
import ast.data.type.base.RangeType;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.composed.UnsafeUnionType;
import ast.data.type.special.NaturalType;
import ast.data.type.special.VoidType;
import ast.repository.manipulator.TypeRepo;
import ast.traverser.NullTraverser;

public class Supertype extends NullTraverser<Type, Void> {
  private TypeRepo kbi;

  public Supertype(KnowledgeBase kb) {
    super();
    kbi = new TypeRepo(kb);
  }

  static public Type get(Type typ, KnowledgeBase kb) {
    Supertype supertype = new Supertype(kb);
    return supertype.traverse(typ, null);
  }

  @Override
  protected Type visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
    if (obj.range.low.compareTo(BigInteger.ZERO) >= 0) {
      return kbi.getNaturalType();
    } else {
      return kbi.getIntegerType();
    }
  }

  @Override
  protected Type visitUnionType(UnionType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitUnsafeUnionType(UnsafeUnionType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    return kbi.getRangeType(obj.element.size());
  }

  @Override
  protected Type visitVoidType(VoidType obj, Void param) {
    assert (kbi.getVoidType() == obj);
    return kbi.getVoidType();
  }

  @Override
  protected Type visitBooleanType(BooleanType obj, Void param) {
    assert (kbi.getBooleanType() == obj);
    return kbi.getBooleanType();
  }

  @Override
  protected Type visitNaturalType(NaturalType obj, Void param) {
    return kbi.getIntegerType();
  }

}
