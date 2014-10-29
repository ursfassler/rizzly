package fun.type.template;

import common.ElementInfo;

import fun.other.FunList;
import fun.type.base.AnyType;
import fun.variable.TemplateParameter;

public class TypeTypeTemplate extends TypeTemplate {
  public static final String NAME = "Type";
  public static final String[] PARAM = { "T" };

  public TypeTypeTemplate() {
    super(ElementInfo.NO);
  }

  public static FunList<TemplateParameter> makeParam() {
    FunList<TemplateParameter> ret = new FunList<TemplateParameter>();
    ret.add(inst(PARAM[0], AnyType.NAME));
    return ret;
  }

}
