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

package evl.pass.check.type;

import java.math.BigInteger;

import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.BooleanType;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;

public class Supertype extends NullTraverser<Type, Void> {
  private KnowBaseItem kbi;

  public Supertype(KnowledgeBase kb) {
    super();
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  static public Type get(Type typ, KnowledgeBase kb) {
    Supertype supertype = new Supertype(kb);
    return supertype.traverse(typ, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitRangeType(RangeType obj, Void param) {
    if (obj.range.getLow().compareTo(BigInteger.ZERO) >= 0) {
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
    return kbi.getRangeType(obj.getElement().size());
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
