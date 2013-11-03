package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.hfsm.Transition;

public class CopyStateItem extends NullTraverser<StateItem, Void> {
  private CopyEvl cast;

  public CopyStateItem(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected StateItem visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected StateItem visitTransition(Transition obj, Void param) {
    // State src = cast.copy(obj.getSrcS()); //TODO correct?
    // State dst = cast.copy(obj.getDstS());
    State src = obj.getSrc(); // TODO correct?
    State dst = obj.getDst();
    return new Transition(obj.getInfo(), obj.getName(), src, dst, cast.copy(obj.getEventFunc()), cast.copy(obj.getGuard()), cast.copy(obj.getParam().getList()), cast.copy(obj.getBody()));
  }

}
