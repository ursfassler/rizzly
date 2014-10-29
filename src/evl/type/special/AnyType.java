package evl.type.special;

import common.ElementInfo;

import evl.type.base.BaseType;

public class AnyType extends BaseType {
  public static final String NAME = "Any";

  public AnyType() {
    super(ElementInfo.NO, NAME);
  }
}
