package evl.type.base;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.type.Type;


public class EnumType extends Type {
  private List<EnumElement> element;

  public EnumType(ElementInfo info, String name) {
    super(info, name);
    this.element = new ArrayList<EnumElement>();
  }

  public List<EnumElement> getElement() {
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

}
