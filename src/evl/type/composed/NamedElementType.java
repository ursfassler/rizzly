package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.type.Type;

abstract public class NamedElementType extends Type {
  final private ListOfNamed<NamedElement> element;

  public NamedElementType(ElementInfo info, String name, Collection<NamedElement> element) {
    super(info, name);
    this.element = new ListOfNamed<NamedElement>(element);
  }

  public int getSize() {
    return element.size();
  }

  public ListOfNamed<NamedElement> getElement() {
    return element;
  }

}
