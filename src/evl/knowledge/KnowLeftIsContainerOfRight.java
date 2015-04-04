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
import util.Range;
import evl.data.Evl;
import evl.data.expression.reference.SimpleRef;
import evl.data.type.Type;
import evl.data.type.base.ArrayType;
import evl.data.type.base.BooleanType;
import evl.data.type.base.EnumElement;
import evl.data.type.base.EnumType;
import evl.data.type.base.FunctionType;
import evl.data.type.base.RangeType;
import evl.data.type.base.StringType;
import evl.data.type.base.TupleType;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.type.composed.UnionType;
import evl.data.type.composed.UnsafeUnionType;
import evl.data.type.out.SIntType;
import evl.data.type.out.UIntType;
import evl.data.type.special.AnyType;
import evl.data.type.special.IntegerType;
import evl.data.type.special.NaturalType;
import evl.data.type.special.VoidType;
import evl.pass.check.type.Supertype;
import evl.traverser.NullTraverser;

public class KnowLeftIsContainerOfRight extends KnowledgeEntry {
  final private HashMap<Pair<Type, Type>, Boolean> cache = new HashMap<Pair<Type, Type>, Boolean>();
  private KnowLeftIsContainerOfRightWorker worker;

  @Override
  public void init(KnowledgeBase base) {
    worker = new KnowLeftIsContainerOfRightWorker(base);
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

  public boolean areEqual(Type left, Type right) {
    return get(left, right) && get(right, left);
  }

}

class KnowLeftIsContainerOfRightWorker extends NullTraverser<Boolean, Type> {
  private KnowledgeBase kb;

  public KnowLeftIsContainerOfRightWorker(KnowledgeBase kb) {
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
      Type lefttype = left.get(i).link;
      Type righttype = right.get(i).link;
      if (!visit(lefttype, righttype)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitFunctionType(FunctionType left, Type right) {
    Type leftret = left.ret.link;
    Type rightret = ((FunctionType) right).ret.link;
    return visit(leftret, rightret) && process(left.arg, ((FunctionType) right).arg);
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
    return true;
  }

  @Override
  protected Boolean visitUIntType(UIntType left, Type right) {
    if (right instanceof UIntType) {
      return left.bytes <= ((UIntType) right).bytes;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitSIntType(SIntType left, Type right) {
    if (right instanceof SIntType) {
      return left.bytes <= ((SIntType) right).bytes;
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitRangeType(RangeType left, Type right) {
    if (right instanceof RangeType) {
      Range lr = left.range;
      Range rr = ((RangeType) right).range;

      return Range.leftIsSmallerEqual(rr, lr); // TODO test
    } else {
      return false; // TODO correct?
    }
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
      return process(obj.types, ((TupleType) right).types);
    } else if (right instanceof RecordType) {
      List<SimpleRef<Type>> rtypes = new ArrayList<SimpleRef<Type>>();
      for (NamedElement elem : ((RecordType) right).element) {
        rtypes.add(elem.ref);
      }
      return process(obj.types, rtypes);
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
      for (NamedElement elem : left.element) {
        lt.add(elem.ref);
      }
      return process(lt, ((TupleType) right).types);
    } else {
      return false; // TODO check if left is supertype of right
    }
  }

  @Override
  protected Boolean visitArrayType(ArrayType left, Type right) {
    if (right instanceof ArrayType) {
      Type lefttype = left.type.link;
      Type righttype = ((ArrayType) right).type.link;
      if (!visit(lefttype, righttype)) {
        return false;
      }
      return left.size.compareTo(((ArrayType) right).size) <= 0;
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
