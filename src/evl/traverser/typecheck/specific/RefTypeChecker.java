package evl.traverser.typecheck.specific;

import java.util.List;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.RefPtrDeref;
import evl.expression.reference.Reference;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.LeftIsContainerOfRightTest;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.ArrayType;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.special.PointerType;

//TODO check if this class does not too much (i.e. check and return type)
public class RefTypeChecker extends NullTraverser<Type, Type> {
  private KnowledgeBase kb;
  private KnowBaseItem kbi;
  private KnowType kt;

  public RefTypeChecker(KnowledgeBase kb) {
    super();
    this.kb = kb;
    kbi = kb.getEntry(KnowBaseItem.class);
    kt = kb.getEntry(KnowType.class);
  }

  static public void process(Reference ast, KnowledgeBase kb) {
    RefTypeChecker adder = new RefTypeChecker(kb);
    adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitReference(Reference obj, Type param) {
    Type ret = kt.get(obj.getLink());
    for (RefItem ref : obj.getOffset()) {
      ret = visit(ref, ret);
      assert (ret != null);
    }
    return ret;
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type sub) {
    if (sub instanceof FunctionType) {
      List<TypeRef> arg = ((FunctionType) sub).getArgD();
      if (arg.size() != obj.getActualParameter().size()) {
        RError.err(ErrorType.Error, obj.getInfo(), "Need " + arg.size() + " arguments, got " + obj.getActualParameter().size());
        return null;
      }
      for (int i = 0; i < arg.size(); i++) {
        Type partype = arg.get(i).getRef();
        Type valtype = kt.get(obj.getActualParameter().get(i));
        if (!LeftIsContainerOfRightTest.process(partype, valtype, kb)) {
          RError.err(ErrorType.Error, obj.getActualParameter().get(i).getInfo(), "Data type to big or incompatible (argument " + (i + 1) + ", " + partype.getName() + " := " + valtype.getName() + ")");
        }
      }
      if (sub instanceof FunctionTypeRet) {
        return ((FunctionTypeRet) sub).getRet().getRef();
      } else {
        return kbi.getVoidType();
      }
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "Not a function: " + obj.toString());
      return null;
    }
  }

  @Override
  protected Type visitRefName(RefName obj, Type sub) {
    if (sub instanceof EnumType) {
      return sub;
    } else {
      KnowChild kc = kb.getEntry(KnowChild.class);
      Evl etype = kc.find(sub, obj.getName());
      if (etype == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Child not found: " + obj.getName());
      }
      return kt.get(etype);
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    Type index = kt.get(obj.getIndex());
    if (sub instanceof ArrayType) {
      if (!LeftIsContainerOfRightTest.process(kbi.getIntegerType(), index, kb)) {
        RError.err(ErrorType.Error, obj.getInfo(), "need integer type to index array, got: " + index.getName());
      }
      return ((ArrayType) sub).getType().getRef();
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "need array to index, got type: " + sub.getName());
      return null;
    }
  }

  @Override
  protected Type visitRefPtrDeref(RefPtrDeref obj, Type param) {
    assert (param instanceof PointerType);
    return ((PointerType) param).getType().getRef();
  }

}
