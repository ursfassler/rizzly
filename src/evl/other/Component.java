package evl.other;

import common.Direction;
import common.ElementInfo;

import evl.EvlBase;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.function.header.FuncCtrlInDataIn;
import evl.function.header.FuncCtrlInDataOut;
import evl.function.header.FuncCtrlOutDataIn;
import evl.function.header.FuncCtrlOutDataOut;

abstract public class Component extends EvlBase implements Named {
  private String name;
  private Queue queue;
  final private EvlList<InterfaceFunction> iface = new EvlList<InterfaceFunction>();
  final private EvlList<Function> function = new EvlList<Function>();

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    queue = new Queue();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EvlList<Function> getFunction() {
    return function;
  }

  public EvlList<InterfaceFunction> getIface(Direction dir) {
    EvlList<InterfaceFunction> ret = new EvlList<InterfaceFunction>();
    switch (dir) {
      case in: {
        ret.addAll(iface.getItems(FuncCtrlInDataOut.class));
        ret.addAll(iface.getItems(FuncCtrlInDataIn.class));
        break;
      }
      case out: {
        ret.addAll(iface.getItems(FuncCtrlOutDataIn.class));
        ret.addAll(iface.getItems(FuncCtrlOutDataOut.class));
        break;
      }
      default:
        throw new RuntimeException("Not implemented: " + dir);
    }
    return ret;
  }

  public EvlList<InterfaceFunction> getIface() {
    return iface;
  }

  public Queue getQueue() {
    return queue;
  }

  public void setQueue(Queue queue) {
    this.queue = queue;
  }

}
