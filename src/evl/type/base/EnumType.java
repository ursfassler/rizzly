package evl.type.base;

import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.type.Type;

public class EnumType extends Type {
  final private ListOfNamed<EnumElement> element = new ListOfNamed<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public ListOfNamed<EnumElement> getElement() {
    return element;
  }

  public EnumElement find(String name) {
    for (EnumElement itr : element) {
      if (itr.getName().equals(name)) {
        return itr;
      }
    }
    return null;
  }

  public boolean isSupertypeOf(EnumType sub) {
    return element.getList().containsAll(sub.element.getList());
  }

}
