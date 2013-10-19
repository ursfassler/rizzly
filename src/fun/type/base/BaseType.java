package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.type.Type;
import fun.variable.TemplateParameter;

abstract public class BaseType extends Type {
  public BaseType(ElementInfo info, String name) {
    super(info, name);
  }

  abstract public ListOfNamed<TemplateParameter> getParamList();

}
