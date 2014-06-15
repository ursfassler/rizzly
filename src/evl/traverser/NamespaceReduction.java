package evl.traverser;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FunctionBase;
import evl.function.impl.FuncProtoRet;
import evl.other.ListOfNamed;
import evl.other.Named;
import evl.other.Namespace;
import evl.other.Queue;
import evl.other.SubCallbacks;
import evl.type.Type;
import evl.variable.Constant;
import evl.variable.StateVariable;

// reduces names of named objects in named lists
public class NamespaceReduction extends NullTraverser<Void, Designator> {
  private ListOfNamed<Named> list = new ListOfNamed<Named>();

  public static ListOfNamed<Named> process(Namespace names) {
    NamespaceReduction reducer = new NamespaceReduction();
    reducer.visitList(names, new Designator());
    return reducer.list;
  }

  private void visitList(Iterable<? extends Named> list, Designator param) {
    for (Named itr : list) {
      visit(itr, param);
    }
  }

  private void addToList(Designator param, Named itr) {
    Designator des = new Designator(param, itr.getName());
    itr.setName(des.toString(Designator.NAME_SEP));
    this.list.add(itr);
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitList(obj, param);
    return null;
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitList(obj.getList(), param);
    return null;
  }

  @Override
  protected Void visitFunctionBase(FunctionBase obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitConstant(Constant obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitFuncProtoRet(FuncProtoRet obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitType(Type obj, Designator param) {
    addToList(param, obj);
    return null;
  }

  @Override
  protected Void visitQueue(Queue obj, Designator param) {
    addToList(param, obj);
    return null;
  }

}
