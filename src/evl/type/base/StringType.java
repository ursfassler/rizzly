package evl.type.base;

import common.ElementInfo;

import evl.Evl;

public class StringType extends BaseType implements Evl {
  public static final String NAME = "String";

  public StringType() {
    super(ElementInfo.NO, NAME);
  }

}
