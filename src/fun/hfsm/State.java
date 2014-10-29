package fun.hfsm;

import common.ElementInfo;

import fun.FunBase;
import fun.other.FunList;
import fun.other.Named;
import fun.statement.Block;

abstract public class State extends FunBase implements Named, StateContent {
  private String name;
  private Block entryFunc = null;
  private Block exitFunc = null;
  final protected FunList<StateContent> item = new FunList<StateContent>();

  public State(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Block getEntryFunc() {
    return entryFunc;
  }

  public void setEntryFunc(Block entryFunc) {
    this.entryFunc = entryFunc;
  }

  public Block getExitFunc() {
    return exitFunc;
  }

  public void setExitFunc(Block exitFunc) {
    this.exitFunc = exitFunc;
  }

  public FunList<StateContent> getItemList() {
    return item;
  }

}
