package fun.type.base;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.Type;

public class EnumType extends Type {
  final private FunList<EnumElement> elements = new FunList<EnumElement>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public FunList<EnumElement> getElement() {
    return elements;
  }

}
