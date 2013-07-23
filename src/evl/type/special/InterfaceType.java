package evl.type.special;

import common.ElementInfo;

import evl.other.ListOfNamed;
import evl.type.base.BaseType;
import evl.type.base.FunctionType;

public class InterfaceType extends BaseType {
  final private ListOfNamed<FunctionType> prototype = new ListOfNamed<FunctionType>();

  public InterfaceType(ElementInfo info, String name) {
    super(info, name);
  }

  public ListOfNamed<FunctionType> getPrototype() {
    return prototype;
  }

}
