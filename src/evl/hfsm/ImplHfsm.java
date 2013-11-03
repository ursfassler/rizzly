package evl.hfsm;

import common.ElementInfo;

import evl.other.Component;

public class ImplHfsm extends Component {
  private StateComposite topstate = null;

  public ImplHfsm(ElementInfo info, String name) {
    super(info, name);
  }

  public StateComposite getTopstate() {
    assert (topstate != null);
    return topstate;
  }

  public void setTopstate(StateComposite topstate) {
    assert (topstate != null);
    this.topstate = topstate;
  }

}
