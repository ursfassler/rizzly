package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.function.FunctionBase;
import evl.function.impl.FuncPrivateRet;
import evl.function.impl.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.Transition;


/**
 * Moves all functions of all states to the top-state.
 *
 * @author urs
 *
 */
public class StateFuncUplifter extends NullTraverser<Void, Designator> {
  final private List<FunctionBase> func = new ArrayList<FunctionBase>();

  static public void process(ImplHfsm obj) {
    StateFuncUplifter know = new StateFuncUplifter();
    know.traverse(obj, null);
  }

  public List<FunctionBase> getFunc() {
    return func;
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.getTopstate(), new Designator());
    obj.getTopstate().getFunction().addAll(func);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitQueryItem(QueryItem obj, Designator param) {
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    // visit(obj.getEntryCode(), param);//TODO correct? It is no longer a function and should not exist at this point
    // visit(obj.getExitCode(), param);
    visitItr(obj.getFunction(), param);
    visitItr(obj.getItem(), param);

    obj.getFunction().clear();

    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Designator param) {
    param = new Designator(param, obj.getName());
    obj.setName(param.toString(Designator.NAME_SEP));
    func.add(obj);
    return null;
  }
}
