package fun.generator;

import java.util.List;

import common.ElementInfo;

import fun.other.Interface;
import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
public class InterfaceGenerator extends Generator {
  final private Interface template;

  public InterfaceGenerator(ElementInfo info, String name, List<TemplateParameter> param, Interface template) {
    super(info, name, param);
    this.template = template;
  }

  public Interface getTemplate() {
    return template;
  }

}
