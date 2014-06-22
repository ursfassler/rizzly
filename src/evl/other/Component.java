package evl.other;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.EvlBase;
import evl.function.FuncIface;
import evl.function.impl.FuncIfaceInRet;
import evl.function.impl.FuncIfaceInVoid;
import evl.function.impl.FuncIfaceOutRet;
import evl.function.impl.FuncIfaceOutVoid;

abstract public class Component extends EvlBase implements Named {
  private String name;
  final private ListOfNamed<FuncIfaceOutRet> query = new ListOfNamed<FuncIfaceOutRet>();
  final private ListOfNamed<FuncIfaceInRet> response = new ListOfNamed<FuncIfaceInRet>();
  final private ListOfNamed<FuncIfaceOutVoid> signal = new ListOfNamed<FuncIfaceOutVoid>();
  final private ListOfNamed<FuncIfaceInVoid> slot = new ListOfNamed<FuncIfaceInVoid>();
  private Queue queue;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    queue = new Queue(Designator.NAME_SEP + Queue.DEFAULT_NAME + Designator.NAME_SEP + name);
  }

  public ListOfNamed<FuncIfaceOutRet> getQuery() {
    return query;
  }

  public ListOfNamed<FuncIfaceInRet> getResponse() {
    return response;
  }

  public ListOfNamed<FuncIfaceOutVoid> getSignal() {
    return signal;
  }

  public ListOfNamed<FuncIfaceInVoid> getSlot() {
    return slot;
  }

  public ListOfNamed<? extends FuncIface> getIface(Direction dir) {
    switch (dir) {
    case in: {
      ListOfNamed<FuncIface> ret = new ListOfNamed<FuncIface>();
      ret.addAll(response);
      ret.addAll(slot);
      return ret;
    }
    case out: {
      ListOfNamed<FuncIface> ret = new ListOfNamed<FuncIface>();
      ret.addAll(query);
      ret.addAll(signal);
      return ret;
    }
    default:
      throw new RuntimeException("Not implemented: " + dir);
    }
  }

  public ListOfNamed<? extends FuncIface> getIface() {
    ListOfNamed<FuncIface> ret = new ListOfNamed<FuncIface>();
    ret.addAll(response);
    ret.addAll(slot);
    ret.addAll(query);
    ret.addAll(signal);
    return ret;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public Queue getQueue() {
    return queue;
  }

  public void setQueue(Queue queue) {
    this.queue = queue;
  }

  @Override
  public String toString() {
    return name;
  }

}
