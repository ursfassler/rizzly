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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.Pair;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.SimpleRef;
import evl.pass.check.type.Supertype;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.base.TupleType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.composed.UnsafeUnionType;
import evl.type.out.SIntType;
import evl.type.out.UIntType;
import evl.type.special.AnyType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;

public class KnowComparable extends KnowledgeEntry {
  final private HashMap<Pair<Type, Type>, Boolean> cache = new HashMap<Pair<Type, Type>, Boolean>();
  private KnowComparableWorker worker;

  @Override
  public void init(KnowledgeBase base) {
    worker = new KnowComparableWorker(base);
  }

  public boolean get(Type left, Type right) {
    if (right instanceof AnyType) {
      return true;
    }

    Pair<Type, Type> pair = new Pair<Type, Type>(left, right);
    if (!cache.containsKey(pair)) {
      cache.put(pair, worker.traverse(left, right));
    }
    return cache.get(pair);
  }

}

class KnowComparableWorker extends NullTraverser<Boolean, Type> {
  private KnowledgeBase kb;

  public KnowComparableWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Boolean visitDefault(Evl left, Type right) {
    throw new RuntimeException("not yet implemented: " + left.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visit(Evl left, Type right) {
    return super.visit(left, right);
  }

  private Boolean isDerivativeOf(Class<? extends Type> baseClass, Type type) {
    while (!(baseClass.isInstance(type))) {
      Type parent = getSupertype(type);
      if (parent == type) {
        return false;
      }
      type = parent;
    }
    return true;
  }

  private Type getSupertype(Type right) {
    return Supertype.get(right, kb);
  }

  private boolean process(List<SimpleRef<Type>> left, List<SimpleRef<Type>> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      Type lefttype = left.get(i).getLink();
      Type righttype = right.get(i).getLink();
      if (!visit(lefttype, righttype)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitNaturalType(NaturalType left, Type right) {
    return isDerivativeOf(left.getClass(), right);
  }

  @Override
  protected Boolean visitIntegerType(IntegerType left, Type right) {
    return isDerivativeOf(left.getClass(), right);
  }

  @Override
  protected Boolean visitVoidType(VoidType left, Type right) {
    return false;
  }

  @Override
  protected Boolean visitUIntType(UIntType left, Type right) {
    return isDerivativeOf(IntegerType.class, right);
  }

  @Override
  protected Boolean visitSIntType(SIntType left, Type right) {
    return isDerivativeOf(IntegerType.class, right);
  }

  @Override
  protected Boolean visitRangeType(RangeType left, Type right) {
    return isDerivativeOf(IntegerType.class, right);
  }

  @Override
  protected Boolean visitBooleanType(BooleanType left, Type right) {
    return right instanceof BooleanType;
  }

  @Override
  protected Boolean visitStringType(StringType left, Type right) {
    return right instanceof StringType;
  }

  @Override
  protected Boolean visitTupleType(TupleType obj, Type right) {
    if (right instanceof TupleType) {
      return process(obj.getTypes(), ((TupleType) right).getTypes());
    } else if (right instanceof RecordType) {
      List<SimpleRef<Type>> rtypes = new ArrayList<SimpleRef<Type>>();
      for (NamedElement elem : ((RecordType) right).getElement()) {
        rtypes.add(elem.getRef());
      }
      return process(obj.getTypes(), rtypes);
    } else {
      throw new RuntimeException("not yet implemented: " + right);
    }
  }

  @Override
  protected Boolean visitRecordType(RecordType left, Type right) {
    if (left.equals(right)) {
      return true;
    } else if (right instanceof TupleType) {
      List<SimpleRef<Type>> lt = new ArrayList<SimpleRef<Type>>();
      for (NamedElement elem : left.getElement()) {
        lt.add(elem.getRef());
      }
      return process(lt, ((TupleType) right).getTypes());
    } else {
      return false; // TODO check if left is supertype of right
    }
  }

  @Override
  protected Boolean visitArrayType(ArrayType left, Type right) {
    if (right instanceof ArrayType) {
      Type lefttype = left.getType().getLink();
      Type righttype = ((ArrayType) right).getType().getLink();
      if (!visit(lefttype, righttype)) {
        return false;
      }
      return left.getSize().compareTo(((ArrayType) right).getSize()) <= 0;
    } else if (right instanceof TupleType) {
      List<SimpleRef<Type>> lt = new ArrayList<SimpleRef<Type>>();
      for (int i = 0; i < left.getSize().intValue(); i++) {
        lt.add(left.getType());
      }
      return process(lt, ((TupleType) right).getTypes());
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitUnionType(UnionType left, Type right) {
    return left == right;  // XXX is this correct?
  }

  @Override
  protected Boolean visitUnsafeUnionType(UnsafeUnionType left, Type right) {
    return left == right;  // XXX is this correct?
  }

  @Override
  protected Boolean visitEnumType(EnumType left, Type right) {
    return left.equals(right);
  }

  @Override
  protected Boolean visitEnumElement(EnumElement left, Type right) {
    throw new RuntimeException("not yet implemented");
  }
}
