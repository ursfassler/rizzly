package fun.type.base;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.type.Type;

public class EnumType extends Type {
  final private List<EnumElement> elements = new ArrayList<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public List<EnumElement> getElement() {
    return elements;
  }

  public EnumElement createElement(String name, ElementInfo info) {
    assert (find(name) == null);
    EnumElement elem = new EnumElement(info, name, this, BigInteger.valueOf(elements.size()));
    elements.add(elem);
    return elem;
  }

  public EnumElement find(String name) {
    for (EnumElement elem : elements) {
      if (name.equals(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

}
