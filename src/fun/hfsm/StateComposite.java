package fun.hfsm;

import common.ElementInfo;

import fun.expression.reference.Reference;

public class StateComposite extends State {
  private Reference initial;

  public StateComposite(ElementInfo info, String name, Reference initial) {
    super(info, name);
    this.initial = initial;
  }

  public Reference getInitial() {
    return initial;
  }

  public void setInitial(Reference initial) {
    this.initial = initial;
  }

}
