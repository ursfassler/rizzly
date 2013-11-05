package evl.type.special;

import java.util.Collection;

import common.ElementInfo;

import evl.type.composed.NamedElement;
import evl.type.composed.NamedElementType;

public class ComponentType extends NamedElementType {

  public ComponentType(ElementInfo info, String name, Collection<NamedElement> element) {
    super(info, name, element);
  }

}
