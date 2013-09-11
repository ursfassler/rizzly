package evl.other;

import java.util.EnumMap;

import common.Direction;
import common.ElementInfo;

import evl.EvlBase;

abstract public class Component extends EvlBase implements Named {
  private String name;
  final private EnumMap<Direction, ListOfNamed<IfaceUse>> iface;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
    iface = new EnumMap<Direction, ListOfNamed<IfaceUse>>(Direction.class);
    iface.put(Direction.in, new ListOfNamed<IfaceUse>());
    iface.put(Direction.out, new ListOfNamed<IfaceUse>());
  }

  public ListOfNamed<IfaceUse> getIface(Direction dir) {
    return iface.get(dir);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

}
