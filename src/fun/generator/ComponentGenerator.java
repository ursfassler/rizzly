package fun.generator;

import java.util.List;

import common.ElementInfo;

import fun.other.Component;
import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
public class ComponentGenerator extends Generator {
  final private Component template;

  public ComponentGenerator(ElementInfo info, String name, List<TemplateParameter> param, Component template) {
    super(info, name, param);
    this.template = template;
  }

  public Component getTemplate() {
    return template;
  }

}
