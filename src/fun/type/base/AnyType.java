package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.variable.TemplateParameter;

public class AnyType extends BaseType {
  public static final String NAME = "Any";

  public AnyType() {
    super(new ElementInfo(), NAME);
  }

  @Override
  public ListOfNamed<TemplateParameter> getParamList() {
    return new ListOfNamed<TemplateParameter>();
  }

}
