package evl.copy;

import java.util.Collection;
import java.util.Map;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.IfaceUse;
import evl.other.Interface;
import evl.other.Named;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;

public class Relinker extends DefTraverser<Void, Void> {
  private Map<? extends Named, ? extends Named> copied;

  static public void relink(Evl obj, Map<? extends Named, ? extends Named> map) {
    Relinker relinker = new Relinker(map);
    relinker.traverse(obj, null);
  }

  static public void relink(Collection<? extends Evl> obj, Map<? extends Named, ? extends Named> map) {
    Relinker relinker = new Relinker(map);
    for (Evl itr : obj) {
      relinker.traverse(itr, null);
    }
  }

  public Relinker(Map<? extends Named, ? extends Named> copied) {
    super();
    this.copied = copied;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    super.visitReference(obj, param);
    if (copied.containsKey(obj.getLink())) {
      obj.setLink(copied.get(obj.getLink()));
    }
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    if (copied.containsKey(obj.getLink())) {
      obj.setLink((Component) copied.get(obj.getLink()));
    }
    return null;
  }

  @Override
  protected Void visitIfaceUse(IfaceUse obj, Void param) {
    if (copied.containsKey(obj.getLink())) {
      obj.setLink((Interface) copied.get(obj.getLink()));
    }
    return null;
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Void param) {
    if (copied.containsKey(obj.getRef())) {
      obj.setRef((Type) copied.get(obj.getRef()));
    }
    return null;
  }

  @Override
  protected Void visitFunctionTypeVoid(FunctionTypeVoid obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Void visitFunctionTypeRet(FunctionTypeRet obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
