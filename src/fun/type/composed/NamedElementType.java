package fun.type.composed;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.type.TypeGenerator;

abstract public class NamedElementType extends TypeGenerator {
  final private ListOfNamed<NamedElement> element = new ListOfNamed<NamedElement>();

  public NamedElementType(ElementInfo info, String name) {
    super(info, name);
  }

  public int getSize() {
    return element.size();
  }

  public ListOfNamed<NamedElement> getElement() {
    return element;
  }

}
