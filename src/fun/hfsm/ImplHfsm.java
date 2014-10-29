package fun.hfsm;

import common.ElementInfo;

import fun.content.CompIfaceContent;
import fun.other.CompImpl;
import fun.other.FunList;

public class ImplHfsm extends CompImpl {
  final private FunList<CompIfaceContent> iface = new FunList<CompIfaceContent>();
  private State topstate;

  public ImplHfsm(ElementInfo info, String name, State topState) {
    super(info, name);
    topstate = topState;
  }

  public State getTopstate() {
    return topstate;
  }

  public void setTopstate(State topstate) {
    this.topstate = topstate;
  }

  @Override
  public FunList<CompIfaceContent> getInterface() {
    return iface;
  }

}
