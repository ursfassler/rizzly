package fun.type.composed;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.Type;

abstract public class NamedElementType extends Type {
  final private FunList<NamedElement> element = new FunList<NamedElement>();

  public NamedElementType(ElementInfo info, String name) {
    super(info, name);
  }

  public int getSize() {
    return element.size();
  }

  public FunList<NamedElement> getElement() {
    return element;
  }

}
