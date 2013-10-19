package fun.generator;

import java.util.List;

import common.ElementInfo;

import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
abstract public class TypeGenerator extends Generator {

  public TypeGenerator(ElementInfo info, String name, List<TemplateParameter> param) {
    super(info, name, param);
  }
}
