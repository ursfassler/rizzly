package evl.hfsm.reduction;

import java.util.HashMap;
import java.util.Map;

import common.Designator;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.hfsm.QueryItem;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.variable.StateVariable;


public class StateVarPathGetter extends NullTraverser<Void, Designator> {
  final private Map<StateVariable, Designator> vpath = new HashMap<StateVariable, Designator>();

  public Map<StateVariable, Designator> getVpath() {
    return vpath;
  }

  public void process(ImplHfsm obj) {
    visitItr(obj.getTopstate().getVariable(), new Designator());
    visitItr(obj.getTopstate().getItem(), new Designator(StateTypeBuilder.SUB_ENTRY_NAME));
  }

  @Override
  protected Void visitDefault(Evl obj, Designator param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.getName());
    visitItr(obj.getVariable(), param);
    param = new Designator(param, StateTypeBuilder.SUB_ENTRY_NAME);
    visitItr(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Designator param) {
    param = new Designator(param, obj.getName());
    vpath.put(obj, param);
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

}
