package evl.other;

import common.Designator;
import common.Direction;
import common.ElementInfo;

import evl.EvlBase;
import evl.function.FuncIface;
import evl.function.FuncIfaceIn;
import evl.function.FuncIfaceOut;

abstract public class Component extends EvlBase implements Named {
  private String name;
  final private ListOfNamed<FuncIfaceIn> input = new ListOfNamed<FuncIfaceIn>();
  final private ListOfNamed<FuncIfaceOut> output = new ListOfNamed<FuncIfaceOut>();
  private Queue queue;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    queue = new Queue(Designator.NAME_SEP + Queue.DEFAULT_NAME + Designator.NAME_SEP + name);
  }

  public ListOfNamed<FuncIfaceIn> getInput() {
    return input;
  }

  public ListOfNamed<FuncIfaceOut> getOutput() {
    return output;
  }

  public ListOfNamed<? extends FuncIface> getIface(Direction dir) {
    switch (dir) {
    case in:
      return input;
    case out:
      return output;
    default:
      throw new RuntimeException("Not implemented: " + dir);
    }
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
