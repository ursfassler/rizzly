package fun.other;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import fun.FunBase;
import fun.variable.IfaceUse;

abstract public class Component extends FunBase {
  final private Map<Direction, ListOfNamed<IfaceUse>> iface;

  public Component(ElementInfo info) {
    super(info);
    iface = new HashMap<Direction, ListOfNamed<IfaceUse>>();
    iface.put(Direction.in, new ListOfNamed<IfaceUse>());
    iface.put(Direction.out, new ListOfNamed<IfaceUse>());
  }

  public ListOfNamed<IfaceUse> getIface(Direction dir) {
    return iface.get(dir);
  }

}
