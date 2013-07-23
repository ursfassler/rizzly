package fun.hfsm;

import common.ElementInfo;

import fun.other.Component;

public class ImplHfsm extends Component {
  private StateComposite topstate;

  public ImplHfsm(ElementInfo info, StateComposite topState) {
    super(info);
    topstate = topState;
  }

  public StateComposite getTopstate() {
    return topstate;
  }

  public void setTopstate(StateComposite topstate) {
    this.topstate = topstate;
  }

}
