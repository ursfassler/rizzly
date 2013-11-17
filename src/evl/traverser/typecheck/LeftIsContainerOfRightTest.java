package evl.traverser.typecheck;

import java.util.List;

import util.Range;
import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
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
    return super.visit((Type) left, right);
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

  private boolean process(List<TypeRef> left, List<TypeRef> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      Type lefttype = left.get(i).getRef();
      Type righttype = right.get(i).getRef();
      if (!visit(lefttype, righttype)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitFunctionTypeRet(FunctionTypeRet left, Type right) {
    if (right instanceof FunctionTypeRet) {
      Type leftret = left.getRet().getRef();
      Type rightret = ((FunctionTypeRet) right).getRet().getRef();
      return visit(leftret, rightret) && process(((FunctionType) right).getArgD(), left.getArgD());
    } else {
      return false;
    }
  }

  @Override
  protected Boolean visitFunctionTypeVoid(FunctionTypeVoid left, Type right) {
    if (right instanceof FunctionTypeVoid) {
      return process(((FunctionTypeVoid) right).getArgD(), left.getArgD());
    } else {
      return false;
    }
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
  protected Boolean visitNumSet(RangeType obj, Type right) {
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
      Type lefttype = left.getType().getRef();
      Type righttype = ((ArrayType) right).getType().getRef();
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
