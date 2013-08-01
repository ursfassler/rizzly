package fun.type.genfunc;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.BaseType;
import fun.type.base.IntegerType;
import fun.variable.CompfuncParameter;

final public class GenericRange extends BaseType {
  public static final String NAME = "R";
  public static final String[] PARAM = { "low", "high" };

  public GenericRange() {
    super(new ElementInfo());
  }

  public ListOfNamed<CompfuncParameter> getParamList() {
    ListOfNamed<CompfuncParameter> ret = new ListOfNamed<CompfuncParameter>();
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    ret.add(new CompfuncParameter(new ElementInfo(), PARAM[1], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
