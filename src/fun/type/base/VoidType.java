package fun.type.base;

import common.ElementInfo;

public class VoidType extends BaseType {
  public static final String NAME = "Void";
  public static final VoidType INSTANCE = new VoidType();

  public VoidType() {
    super(ElementInfo.NO, NAME);
  }

}
