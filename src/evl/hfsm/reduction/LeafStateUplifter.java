package evl.hfsm.reduction;

import java.util.ArrayList;
import java.util.List;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;

/**
 * Moves all leaf-states up. In the end, the top state only has former leaf states a children.
 * 
 * (The leaf states are the only states the state machoine can be in.)
 * 
 * @author urs
 * 
 */
public class LeafStateUplifter extends NullTraverser<Void, Designator> {
  final private List<StateSimple> states = new ArrayList<StateSimple>();

  static public void process(ImplHfsm obj) {
    LeafStateUplifter know = new LeafStateUplifter();
    know.traverse(obj, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.getTopstate(), new Designator());
    obj.getTopstate().getItem().addAll(states);
    return null;
  }

  @Override
  protected Void visitStateSimple(StateSimple obj, Designator param) {
    obj.setName(param.toString(Designator.NAME_SEP));
    states.add(obj);
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, Designator param) {
    List<State> children = obj.getItemList(State.class);
    visitItr(children, param);
    obj.getItem().removeAll(children);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    return super.visitState(obj, param);
  }

}
