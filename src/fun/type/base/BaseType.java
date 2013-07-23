package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.type.Type;
import fun.variable.CompfuncParameter;

abstract public class BaseType extends Type {
  public BaseType(ElementInfo info) {
    super(info);
  }

  abstract public String getName();

  abstract public ListOfNamed<CompfuncParameter> getParamList();

}
