package evl.traverser.debug;

import java.util.HashSet;
import java.util.Set;

import common.Direction;

import evl.DefTraverser;
import evl.Evl;
import evl.composition.ImplComposition;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;
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
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Set<String> param) {
    visitList(obj.getIface(Direction.in), param);
    visitList(obj.getIface(Direction.out), param);
    visitList(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutVoid(FuncCtrlOutDataOut obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceOutRet(FuncCtrlOutDataIn obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInVoid(FuncCtrlInDataIn obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

  @Override
  protected Void visitFuncIfaceInRet(FuncCtrlInDataOut obj, Set<String> param) {
    param.add(obj.getName());
    return null;
  }

}
