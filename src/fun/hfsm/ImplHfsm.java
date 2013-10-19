package fun.hfsm;

import common.ElementInfo;

import fun.other.Component;

public class ImplHfsm extends Component {
  private StateComposite topstate;

  public ImplHfsm(ElementInfo info, String name, StateComposite topState) {
    super(info,name);
    topstate = topState;
  }

  public StateComposite getTopstate() {
    return topstate;
  }

  public void setTopstate(StateComposite topstate) {
    this.topstate = topstate;
  }

}
