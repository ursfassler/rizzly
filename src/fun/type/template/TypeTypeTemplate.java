package fun.type.template;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.other.ListOfNamed;
import fun.type.base.AnyType;
import fun.type.base.BaseType;
import fun.variable.TemplateParameter;

public class TypeTypeTemplate extends BaseType {
  public static final String NAME = "Type";
  public static final String[] PARAM = { "T" };

  public TypeTypeTemplate() {
    super(new ElementInfo());
  }

  public ListOfNamed<TemplateParameter> getParamList() {
    ListOfNamed<TemplateParameter> ret = new ListOfNamed<TemplateParameter>();
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(AnyType.NAME))));
    return ret;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
