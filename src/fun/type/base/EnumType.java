package fun.type.base;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.Type;

public class EnumType extends Type {
  final private List<Reference> elements = new ArrayList<Reference>();

  public EnumType(ElementInfo info, String name) {
    super(info, name);
  }

  public List<Reference> getElement() {
    return elements;
  }

}
