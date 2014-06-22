package evl.hfsm.reduction;

import evl.Evl;
import evl.NullTraverser;
import evl.function.impl.FuncImplResponse;
import evl.hfsm.State;
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
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Void visitFuncImplResponse(FuncImplResponse obj, Void param) {
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitItr(obj.getItem(), null);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State dst = obj.getDst();
    dst = initStateGetter.traverse(dst, null);
    obj.setDst(dst);
    return null;
  }

}
