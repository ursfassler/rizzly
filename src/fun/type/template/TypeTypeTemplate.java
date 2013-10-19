package fun.type.template;

import java.util.ArrayList;
import java.util.List;

import common.Designator;
import common.ElementInfo;

import fun.expression.reference.ReferenceUnlinked;
import fun.generator.TypeGenerator;
import fun.type.base.AnyType;
import fun.variable.TemplateParameter;

public class TypeTypeTemplate extends TypeGenerator {
  public static final String NAME = "Type";
  public static final String[] PARAM = { "T" };

  public TypeTypeTemplate() {
    super(new ElementInfo(), NAME, makeParam());
  }

  private static List<TemplateParameter> makeParam() {
    ArrayList<TemplateParameter> ret = new ArrayList<TemplateParameter>();
    ret.add(new TemplateParameter(new ElementInfo(), PARAM[0], new ReferenceUnlinked(new ElementInfo(), new Designator(AnyType.NAME))));
    return ret;
  }

}
