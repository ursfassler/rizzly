package fun.variable;

import common.ElementInfo;

import fun.expression.reference.Reference;

final public class TemplateParameter extends Variable {

  public TemplateParameter(ElementInfo info, String name, Reference type) {
    super(info, name, type);
  }

}
