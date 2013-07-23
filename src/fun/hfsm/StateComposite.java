package fun.hfsm;

import common.ElementInfo;

import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.expression.reference.ReferenceUnlinked;

public class StateComposite extends State {
  private Reference initial;

  public StateComposite(ElementInfo info, String name, String initial) {
    super(info, name);
    this.initial = new ReferenceUnlinked(info);
    this.initial.getOffset().add(new RefName(info, initial));
  }

  public Reference getInitial() {
    return initial;
  }

  public void setInitial(Reference initial) {
    this.initial = initial;
  }

}
