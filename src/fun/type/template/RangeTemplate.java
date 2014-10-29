package fun.type.template;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.base.IntegerType;
import fun.variable.TemplateParameter;

final public class RangeTemplate extends TypeTemplate {
  public static final String NAME = "R";
  public static final String[] PARAM = { "low", "high" };

  public RangeTemplate() {
    super(ElementInfo.NO);
  }

  static public FunList<TemplateParameter> makeParam() {
    FunList<TemplateParameter> ret = new FunList<TemplateParameter>();
    ret.add(inst(PARAM[0], IntegerType.NAME));
    ret.add(inst(PARAM[1], IntegerType.NAME));
    return ret;
  }

}
