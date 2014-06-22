package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import evl.Evl;
import evl.NullTraverser;
import evl.function.impl.FuncImplResponse;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;
import evl.hfsm.Transition;

/**
 * Moves all transitions of all states to the top-state.
 * 
 * @author urs
 * 
 */
public class TransitionUplifter extends NullTraverser<Void, List<Transition>> {

  static public void process(ImplHfsm obj) {
    TransitionUplifter know = new TransitionUplifter();
    know.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, List<Transition> param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, List<Transition> param) {
    List<Transition> list = new ArrayList<Transition>();
    visit(obj.getTopstate(), list);
    obj.getTopstate().getItem().addAll(list);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, List<Transition> param) {
    return null;
  }

  @Override
  protected Void visitFuncImplResponse(FuncImplResponse obj, List<Transition> param) {
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, List<Transition> param) {
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, List<Transition> param) {
    List<Transition> transList = obj.getItemList(Transition.class);
    param.addAll(transList);
    obj.getItem().removeAll(transList);
    return super.visitState(obj, param);
  }

}
