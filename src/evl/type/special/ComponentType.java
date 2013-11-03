package evl.type.special;

import java.util.HashMap;
import java.util.Map;

import common.Direction;
import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.type.base.BaseType;
import evl.type.base.FunctionType;

public class ComponentType extends BaseType {
  final private Map<Direction, ListOfNamed<FunctionType>> iface;

  public ComponentType(ElementInfo info, String name) {
    super(info, name);
    iface = new HashMap<Direction, ListOfNamed<FunctionType>>();
    iface.put(Direction.in, new ListOfNamed<FunctionType>());
    iface.put(Direction.out, new ListOfNamed<FunctionType>());
  }

  public ListOfNamed<FunctionType> getIface(Direction dir) {
    return iface.get(dir);
  }

}
