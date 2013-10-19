package fun.type.template;

import java.util.List;

import common.ElementInfo;

import fun.generator.TypeGenerator;
import fun.type.Type;
import fun.variable.TemplateParameter;

public class UserTypeGenerator extends TypeGenerator {
  final private Type template;

  public UserTypeGenerator(ElementInfo info, String name, List<TemplateParameter> param, Type template) {
    super(info, name, param);
    this.template = template;
  }

  public Type getTemplate() {
    return template;
  }

}
