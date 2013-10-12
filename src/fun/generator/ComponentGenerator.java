package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.other.Component;
import fun.variable.TemplateParameter;

/**
 *
 * @author urs
 */
public class ComponentGenerator extends Generator<Component> {

  public ComponentGenerator(ElementInfo info, String name, Collection<TemplateParameter> param, Component item) {
    super(info, name, param, item);
  }
}
