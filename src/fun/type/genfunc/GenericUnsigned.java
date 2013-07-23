package fun.type.genfunc;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.BaseType;
import fun.type.base.NaturalType;
import fun.variable.CompfuncParameter;

final public class GenericUnsigned extends BaseType {
  public static final String NAME = "U";
  public static final String[] PARAM = { "bits" };

  public GenericUnsigned() {
    super(new ElementInfo());
  }

  public ListOfNamed<CompfuncParameter> getParamList() {
    ListOfNamed<CompfuncParameter> ret = new ListOfNamed<CompfuncParameter>();
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(NaturalType.NAME))));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
