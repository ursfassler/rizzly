package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;

public class UnsafeUnionType extends NamedElementType {

  public UnsafeUnionType(ElementInfo info, String name, Collection<NamedElement> element) {
    super(info, name, element);
  }

}
