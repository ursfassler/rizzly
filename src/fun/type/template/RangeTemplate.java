package fun.type.template;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class RangeTemplate extends BuiltinTemplate {
  public static final String NAME = "R";
  public static final String[] PARAM = { "low", "high" };

  public RangeTemplate() {
    super(new ElementInfo(), NAME);
    getTemplateParam().addAll(makeParam());
  }

  static public List<TemplateParameter> makeParam() {
    ArrayList<TemplateParameter> ret = new ArrayList<TemplateParameter>();
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[1], new ReferenceUnlinked(new ElementInfo(), new Designator(IntegerType.NAME))));
    return ret;
  }

}
