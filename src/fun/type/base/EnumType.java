package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.type.Type;

public class EnumType extends Type {
  final private ListOfNamed<EnumElement> elements = new ListOfNamed<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public ListOfNamed<EnumElement> getElement() {
    return elements;
  }

}
