package evl.traverser.typecheck;

import java.util.List;

import evl.Evl;
import evl.NullTraverser;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.specific.RefTypeChecker;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.BooleanType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.base.TypeAlias;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;
import evl.type.special.IntegerType;
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
    left = deAlias((Type) left, kb);
    right = deAlias(right, kb);
    return super.visit(left, right);
  }

  public static Type deAlias(Type type, KnowledgeBase kb) {
    while (type instanceof TypeAlias) {
      type = RefTypeChecker.process(((TypeAlias) type).getRef(), kb);
    }
    return type;
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

  private boolean process(List<Type> left, List<Type> right) {
    if (left.size() != right.size()) {
      return false;
    }
    for (int i = 0; i < left.size(); i++) {
      Type lefttype = left.get(i);
      Type righttype = right.get(i);
      if (!visit(lefttype, righttype)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected Boolean visitFunctionTypeRet(FunctionTypeRet left, Type right) {
    if (right instanceof FunctionTypeRet) {
      Type leftret = RefTypeChecker.process(left.getRet(), kb);
      Type rightret = RefTypeChecker.process(((FunctionTypeRet) right).getRet(), kb);
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
  protected Boolean visitIntegerType(IntegerType obj, Type param) {
    return isDerivativeOf(obj.getClass(), param);
  }

  @Override
  protected Boolean visitVoidType(VoidType left, Type right) {
    return true;
  }

  @Override
  protected Boolean visitRange(Range obj, Type right) {
    if (right instanceof Range) {
      int cmpLow = obj.getLow().compareTo(((Range) right).getLow());
      int cmpHigh = obj.getHigh().compareTo(((Range) right).getHigh());
      return (cmpLow <= 0) && (cmpHigh >= 0); // TODO ok?
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
      Type lefttype = RefTypeChecker.process(left.getType(), kb);
      Type righttype = RefTypeChecker.process(((ArrayType) right).getType(), kb);
      if (!visit(lefttype, righttype)) {
        return false;
      }
      return left.getSize() <= ((ArrayType) right).getSize();
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
