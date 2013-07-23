package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;



public class UnionType extends NamedElementType {

  public UnionType(ElementInfo info, String name, Collection<NamedElement> element) {
    super(info, name, element);
  }

}
