package evl.type.base;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.type.Type;

public class EnumType extends Type {
  final private ArrayList<EnumDefRef> element = new ArrayList<EnumDefRef>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public List<EnumDefRef> getElement() {
    return element;
  }

  @Deprecated
  public EnumElement createElement(String name, ElementInfo info) {
    throw new RuntimeException("no longer implemented");
  }

  public EnumElement find(String name) {
    for (EnumDefRef itr : element) {
      if (itr.getElem().getName().equals(name)) {
        return itr.getElem();
      }
    }
    return null;
  }

  public boolean isSupertypeOf(EnumType sub) {
    return element.containsAll(sub.element);
  }

}
