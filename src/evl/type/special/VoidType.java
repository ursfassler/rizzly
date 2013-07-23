package evl.type.special;

import common.ElementInfo;

import evl.type.base.BaseType;

public class VoidType extends BaseType {
  public static final String NAME = "Void";

  public VoidType() {
    super(new ElementInfo(), NAME);
  }
}
