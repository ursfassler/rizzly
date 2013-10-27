package evl.traverser.modelcheck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;
import evl.traverser.ClassGetter;

/**
 * Checks if the states (src,dst) of a transition are reachable from the transition (inner scope)
 * @author urs
 */
public class HfsmTransScopeCheck extends NullTraverser<Set<State>, Void> {

  public static void process(Namespace aclasses, KnowledgeBase kb) {
    HfsmTransScopeCheck check = new HfsmTransScopeCheck();
    List<ImplHfsm> hfsms = ClassGetter.get(ImplHfsm.class, aclasses);
    for( ImplHfsm hfsm : hfsms ) {
      check.traverse(hfsm, null);
    }
  }

  @Override
  protected Set<State> visitDefault(Evl obj, Void param) {
    throw new UnsupportedOperationException("Not supported yet: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Set<State> visitImplHfsm(ImplHfsm obj, Void param) {
    List<State> states = obj.getTopstate().getItemList(State.class);
    for( State subState : states ) {
      visit(subState, null);
    }
    return null;
  }

  @Override
  protected Set<State> visitState(State obj, Void param) {
    Set<State> ret = new HashSet<State>();
    ret.add(obj);

    for( State subState : obj.getItemList(State.class) ) {
      ret.addAll(visit(subState, null));
    }

    for( Transition trans : obj.getItemList(Transition.class) ) {
      checkTransition(trans, ret);
    }

    return ret;
  }

  private void checkTransition(Transition trans, Set<State> allowed) {
    check(trans.getSrc(), allowed, trans.getInfo(), "source");
    check(trans.getDst(), allowed, trans.getInfo(), "destination");
  }

  private void check(State state, Set<State> allowed, ElementInfo info, String end) {
    if( !allowed.contains(state) ) {
      RError.err(ErrorType.Error, info, "Connection to state which is in outer scope for " + end + " (" + state.getName() + ")");
    }
  }
}
