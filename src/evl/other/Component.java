package evl.other;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.EvlBase;

abstract public class Component extends EvlBase implements Named {
  private String name;
  final private Map<Direction, ListOfNamed<IfaceUse>> iface;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    iface = new HashMap<Direction, ListOfNamed<IfaceUse>>();
    iface.put(Direction.in, new ListOfNamed<IfaceUse>());
    iface.put(Direction.out, new ListOfNamed<IfaceUse>());
  }

  public ListOfNamed<IfaceUse> getIface(Direction dir) {
    return iface.get(dir);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
