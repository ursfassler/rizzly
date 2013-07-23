package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.variable.CompfuncParameter;

public class AnyType extends BaseType {
  public static final String NAME = "*";

  public AnyType() {
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
