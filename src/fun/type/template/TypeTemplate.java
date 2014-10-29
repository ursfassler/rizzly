package fun.type.template;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;
import fun.variable.TemplateParameter;

public class TypeTemplate extends FunBase {

  public TypeTemplate(ElementInfo info) {
    super(info);
  }

  static protected TemplateParameter inst(String name, String type) {
    return new TemplateParameter(ElementInfo.NO, name, new Reference(ElementInfo.NO, type));
  }

  protected static TemplateParameter inst(String name, Reference type) {
    return new TemplateParameter(ElementInfo.NO, name, type);
  }
}
