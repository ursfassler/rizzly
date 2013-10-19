package fun.type.composed;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.type.Type;

abstract public class NamedElementType extends Type {
  final private ListOfNamed<NamedElement> element = new ListOfNamed<NamedElement>();

  public NamedElementType(ElementInfo info,String name) {
    super(info,name);
  }

  public int getSize() {
    return element.size();
  }

  public ListOfNamed<NamedElement> getElement() {
    return element;
  }

}
