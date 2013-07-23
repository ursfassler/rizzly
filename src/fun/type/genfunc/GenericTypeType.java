package fun.type.genfunc;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.variable.CompfuncParameter;

public class GenericTypeType extends BaseType {
  public static final String NAME = "Type";
  public static final String[] PARAM = { "T" };

  public GenericTypeType() {
    super(new ElementInfo());
  }

  public ListOfNamed<CompfuncParameter> getParamList() {
    ListOfNamed<CompfuncParameter> ret = new ListOfNamed<CompfuncParameter>();
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(AnyType.NAME))));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
