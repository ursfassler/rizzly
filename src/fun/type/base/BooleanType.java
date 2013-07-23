package fun.type.base;

import common.ElementInfo;

import fun.other.ListOfNamed;
import fun.variable.CompfuncParameter;

final public class BooleanType extends BaseType {
  public BooleanType() {
    super(new ElementInfo());
  }

  @Override
  public String getName() {
    return "Boolean";
  }

  @Override
  public ListOfNamed<CompfuncParameter> getParamList() {
    return new ListOfNamed<CompfuncParameter>();
  }

  @Override
  public int hashCode() {
    return 8234023;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

}
