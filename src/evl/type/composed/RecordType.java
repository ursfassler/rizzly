package evl.type.composed;

import java.util.Collection;

import common.ElementInfo;

public class RecordType extends NamedElementType {

  public RecordType(ElementInfo info, String name, Collection<NamedElement> element) {
    super(info, name, element);
  }

}
