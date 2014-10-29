package evl.traverser;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowChild;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumType;
import evl.type.base.FunctionType;
import evl.type.composed.NamedElement;

public class RefTypeGetter extends NullTraverser<Type, Type> {
  final private KnowChild kc;

  public RefTypeGetter(KnowledgeBase kb) {
    super();
    kc = kb.getEntry(KnowChild.class);
  }

  @Override
  protected Type visitDefault(Evl obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitTypeRef(SimpleRef obj, Type param) {
    return (Type) obj.getLink();
  }

  @Override
  protected Type visitReference(Reference obj, Type param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    // Type ret = kt.traverse(obj.getLink(), null);
    // for (RefItem ref : obj.getOffset()) {
    // ret = visit(ref, ret);
    // assert (ret != null);
    // }
    // return ret;
  }

  @Override
  protected Type visitRefCall(RefCall obj, Type sub) {
    if (sub instanceof FunctionType) {
      return ((FunctionType) sub).getRet().getLink();
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
      String name = obj.getName();
      NamedElement etype = (NamedElement) kc.find(sub, name);
      if (etype == null) {
        RError.err(ErrorType.Error, obj.getInfo(), "Child not found: " + obj);
      }
      return etype.getRef().getLink();
    }
  }

  @Override
  protected Type visitRefIndex(RefIndex obj, Type sub) {
    if (sub instanceof ArrayType) {
      return visit(((ArrayType) sub).getType(), null);
    } else {
      RError.err(ErrorType.Error, obj.getInfo(), "need array to index, got type: " + sub.getName());
      return null;
    }
  }

}
