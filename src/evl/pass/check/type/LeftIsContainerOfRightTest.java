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

import java.util.List;

import util.Range;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.RangeType;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.type.special.VoidType;

public class LeftIsContainerOfRightTest extends NullTraverser<Boolean, Type> {

  private KnowledgeBase kb;

  public LeftIsContainerOfRightTest(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public static boolean process(Type left, Type right, KnowledgeBase kb) {
    LeftIsContainerOfRightTest test = new LeftIsContainerOfRightTest(kb);
    return test.traverse(left, right);
  }

  @Override
  protected Boolean visitDefault(Evl obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Boolean visit(Evl left, Type right) {
    return super.visit(left, right);
  }

  public Boolean isDerivativeOf(Class<? extends Type> baseClass, Type type) {
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
  protected Boolean visitFunctionType(FunctionType left, Type right) {
    Type leftret = left.getRet().getLink();
    Type rightret = ((FunctionType) right).getRet().getLink();
    return visit(leftret, rightret) && process(left.getArg(), ((FunctionType) right).getArg());
  }

  @Override
  protected Boolean visitNaturalType(NaturalType obj, Type param) {
    return isDerivativeOf(obj.getClass(), param);
  }

  @Override
  protected Boolean visitIntegerType(IntegerType obj, Type param) {
    return isDerivativeOf(obj.getClass(), param);
  }

  @Override
  protected Boolean visitVoidType(VoidType left, Type right) {
    return true;
  }

  @Override
  protected Boolean visitRangeType(RangeType obj, Type right) {
    if (right instanceof RangeType) {
      Range lr = obj.getNumbers();
      Range rr = ((RangeType) right).getNumbers();

      return Range.leftIsSmallerEqual(rr, lr); // TODO test
    } else {
      return false; // TODO correct?
    }
  }

  @Override
  protected Boolean visitBooleanType(BooleanType left, Type right) {
    return true;
  }

  @Override
  protected Boolean visitStringType(StringType left, Type right) {
    return true;
  }

  @Override
  protected Boolean visitRecordType(RecordType left, Type right) {
    return left.equals(right); // TODO check if left is supertype of right
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
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitUnionType(UnionType left, Type right) {
    throw new RuntimeException("not yet implemented");
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
