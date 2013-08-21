package evl.traverser.typecheck;

import java.util.ArrayList;
import java.util.List;

import common.Direction;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FuncWithReturn;
import evl.function.FunctionBase;
import evl.other.CompUse;
import evl.other.IfaceUse;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.base.FunctionType;
import evl.type.base.FunctionTypeRet;
import evl.type.base.FunctionTypeVoid;
import evl.type.composed.NamedElement;
import evl.type.special.ComponentType;
import evl.type.special.InterfaceType;
import evl.variable.Variable;

public class TypeGetter extends NullTraverser<Type, Void> {
  static public Type process(Evl ast) {
    TypeGetter adder = new TypeGetter();
    return adder.traverse(ast, null);
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Type visitVariable(Variable obj, Void sym) {
    return visit(obj.getType(), null);
  }

  @Override
  protected Type visitNamedElement(NamedElement obj, Void sym) {
    return visit(obj.getType(), null);
  }

  @Override
  protected Type visitFunctionBase(FunctionBase obj, Void param) {
    List<TypeRef> arg = new ArrayList<TypeRef>(obj.getParam().size());
    for (Variable itr : obj.getParam()) {
      arg.add(itr.getType().copy());
    }
    if (obj instanceof FuncWithReturn) {
      return new FunctionTypeRet(obj.getInfo(), obj.getName(), arg, new TypeRef(obj.getInfo(), ((FuncWithReturn) obj).getRet().getRef()));
    } else {
      return new FunctionTypeVoid(obj.getInfo(), obj.getName(), arg);
    }
  }

  @Override
  protected Type visitType(Type obj, Void param) {
    return obj;
  }

  @Override
  protected Type visitIfaceUse(IfaceUse obj, Void param) {
    InterfaceType ret = new InterfaceType(obj.getInfo(), obj.getName());
    for (FunctionBase func : obj.getLink().getPrototype()) {
      FunctionType ft = (FunctionType) visit(func, null);
      ret.getPrototype().add(ft);
    }
    return ret;
  }

  @Override
  protected Type visitCompUse(CompUse obj, Void param) {
    ComponentType ret = new ComponentType(obj.getInfo(), obj.getName());
    for (IfaceUse iface : obj.getLink().getIface(Direction.out)) {
      InterfaceType it = (InterfaceType) visit(iface, null);
      ret.getIface(Direction.out).add(it);
    }
    for (IfaceUse iface : obj.getLink().getIface(Direction.in)) {
      InterfaceType it = (InterfaceType) visit(iface, null);
      ret.getIface(Direction.in).add(it);
    }
    return ret;
  }

  @Override
  protected Type visitTypeRef(TypeRef obj, Void param) {
    return obj.getRef();
  }

}
