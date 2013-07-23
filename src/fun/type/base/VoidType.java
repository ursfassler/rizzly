package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.variable.CompfuncParameter;

public class VoidType extends BaseType {
  public static final String NAME = "Void";

  public VoidType() {
    super(new ElementInfo());
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ListOfNamed<CompfuncParameter> getParamList() {
    return new ListOfNamed<CompfuncParameter>();
  }

}
