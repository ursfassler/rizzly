package evl.type.special;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.type.base.BaseType;

public class ComponentType extends BaseType {
  final private Map<Direction, ListOfNamed<InterfaceType>> iface;

  public ComponentType(ElementInfo info, String name) {
    super(info, name);
    iface = new HashMap<Direction, ListOfNamed<InterfaceType>>();
    iface.put(Direction.in, new ListOfNamed<InterfaceType>());
    iface.put(Direction.out, new ListOfNamed<InterfaceType>());
  }

  public ListOfNamed<InterfaceType> getIface(Direction dir) {
    return iface.get(dir);
  }

}
