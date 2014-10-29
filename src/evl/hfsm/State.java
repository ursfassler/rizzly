package evl.hfsm;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.function.header.FuncPrivateVoid;
import evl.other.EvlList;
import evl.other.Named;

abstract public class State extends EvlBase implements StateItem, Named {
  private String name;
  final private SimpleRef<FuncPrivateVoid> entryFunc;
  final private SimpleRef<FuncPrivateVoid> exitFunc;
  final protected EvlList<StateItem> item = new EvlList<StateItem>();

  public State(ElementInfo info, String name, SimpleRef<FuncPrivateVoid> entryFunc, SimpleRef<FuncPrivateVoid> exitFunc) {
    super(info);
    this.name = name;
    this.entryFunc = entryFunc;
    this.exitFunc = exitFunc;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SimpleRef<FuncPrivateVoid> getEntryFunc() {
    return entryFunc;
  }

  public SimpleRef<FuncPrivateVoid> getExitFunc() {
    return exitFunc;
  }

  public EvlList<StateItem> getItem() {
    return item;
  }

}
