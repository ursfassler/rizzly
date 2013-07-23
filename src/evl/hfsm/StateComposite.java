package evl.hfsm;

import common.ElementInfo;

public class StateComposite extends State {
  private State initial = null; // we can not set initial when creating the object because it creates a dependency
                                // cycle

  public StateComposite(ElementInfo info, String name) {
    super(info, name);
  }

  public State getInitial() {
    assert (initial != null);
    return initial;
  }

  public void setInitial(State initial) {
    assert (initial != null);
    this.initial = initial;
  }

}
