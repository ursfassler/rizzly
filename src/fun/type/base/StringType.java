package fun.type.base;

import common.ElementInfo;

import evl.Evl;
import fun.other.ListOfNamed;
import fun.variable.TemplateParameter;

public class StringType extends BaseType implements Evl {
  public static final String NAME = "String";

  public StringType() {
    super(new ElementInfo(), NAME);
  }

  @Override
  public ListOfNamed<TemplateParameter> getParamList() {
    return new ListOfNamed<TemplateParameter>();
  }

}
