package fun.type.template;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.BaseType;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class RangeTemplate extends BaseType {
  public static final String NAME = "R";
  public static final String[] PARAM = { "low", "high" };

  public RangeTemplate() {
    super(new ElementInfo());
  }

  public ListOfNamed<TemplateParameter> getParamList() {
    ListOfNamed<TemplateParameter> ret = new ListOfNamed<TemplateParameter>();
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[1], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
