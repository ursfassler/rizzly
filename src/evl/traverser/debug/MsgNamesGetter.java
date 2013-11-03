package evl.traverser.debug;

import java.util.HashSet;
import java.util.Set;

import common.Direction;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;
import evl.function.impl.FuncProtoRet;
import evl.function.impl.FuncProtoVoid;
import evl.other.CompUse;
import evl.other.ImplElementary;

public class MsgNamesGetter extends DefTraverser<Void, Set<String>> {

  public static Set<String> get(Evl obj) {
    Set<String> ret = new HashSet<String>();
    MsgNamesGetter counter = new MsgNamesGetter();
    counter.traverse(obj, ret);
    return ret;
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<String> param) {
    visitItr(obj.getIface(Direction.in), param);
    visitItr(obj.getIface(Direction.out), param);
    visitItr(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Set<String> param) {
    visitItr(obj.getIface(Direction.in), param);
    visitItr(obj.getIface(Direction.out), param);
    visitItr(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutVoid(FuncIfaceOutVoid obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncIfaceOutRet obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncIfaceInVoid obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncIfaceInRet obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncProtoRet(FuncProtoRet obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncProtoVoid(FuncProtoVoid obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

}
