package fun.other;

import java.util.EnumMap;

import common.Direction;
import common.ElementInfo;

import fun.FunBase;
import fun.variable.IfaceUse;

abstract public class Component extends FunBase implements Named {
  final private EnumMap<Direction, ListOfNamed<IfaceUse>> iface;
  private String name;

  public Component(ElementInfo info,String name) {
    super(info);
    this.name = name;
    iface = new EnumMap<Direction, ListOfNamed<IfaceUse>>(Direction.class);
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
