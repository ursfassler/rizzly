package fun.type.template;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.type.base.AnyType;
import fun.variable.TemplateParameter;

public class TypeTypeTemplate extends BuiltinTemplate {
  public static final String NAME = "Type";
  public static final String[] PARAM = { "T" };

  public TypeTypeTemplate() {
    super(new ElementInfo(), NAME);
    getTemplateParam().addAll(makeParam());
  }

  public static List<TemplateParameter> makeParam() {
    ArrayList<TemplateParameter> ret = new ArrayList<TemplateParameter>();
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new Reference(new ElementInfo(), AnyType.NAME)));
    return ret;
  }

}
