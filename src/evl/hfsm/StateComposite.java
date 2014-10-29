package evl.hfsm;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.function.header.FuncPrivateVoid;

public class StateComposite extends State {
  private SimpleRef<State> initial;

  public StateComposite(ElementInfo info, String name, SimpleRef<FuncPrivateVoid> entryFunc, SimpleRef<FuncPrivateVoid> exitFunc, SimpleRef<State> initial) {
    super(info, name, entryFunc, exitFunc);
    this.initial = initial;
  }

  public SimpleRef<State> getInitial() {
    assert (initial != null);
    return initial;
  }

  public void setInitial(SimpleRef<State> initial) {
    assert (initial != null);
    this.initial = initial;
  }

}
