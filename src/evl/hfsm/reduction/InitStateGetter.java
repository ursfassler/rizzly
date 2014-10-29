package evl.hfsm.reduction;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.Reference;
import evl.hfsm.State;
import evl.hfsm.StateComposite;
import evl.hfsm.StateSimple;

/**
 * 
 * Gets the initial leaf state form a state
 * 
 * @author urs
 * 
 */
public class InitStateGetter extends NullTraverser<StateSimple, Void> {
  @Override
  protected StateSimple visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  static public StateSimple get(State obj) {
    InitStateGetter getter = new InitStateGetter();
    return getter.traverse(obj, null);
  }

  @Override
  protected StateSimple visitStateSimple(StateSimple obj, Void param) {
    return obj;
  }

  @Override
  protected StateSimple visitStateComposite(StateComposite obj, Void param) {
    return visit(obj.getInitial().getLink(), param);
  }

  @Override
  protected StateSimple visitReference(Reference obj, Void param) {
    assert (false);
    assert (obj.getOffset().isEmpty());
    return visit(obj.getLink(), param);
  }

}
