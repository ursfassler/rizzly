package evl.hfsm.reduction;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.hfsm.Transition;

/**
 * Sets the destination of a transition to the initial substate if the destination is a composite state
 * 
 * @author urs
 * 
 */
public class TransitionRedirecter extends NullTraverser<Void, Void> {
  final private InitStateGetter initStateGetter = new InitStateGetter();

  public static void process(State top) {
    TransitionRedirecter redirecter = new TransitionRedirecter();
    redirecter.traverse(top, null);
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.getItem(), null);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State dst = obj.getDst().getLink();
    dst = initStateGetter.traverse(dst, null);
    obj.getDst().setLink(dst);
    return null;
  }

}
