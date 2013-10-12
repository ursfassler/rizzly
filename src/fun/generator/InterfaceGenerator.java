package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.other.Interface;
import fun.variable.TemplateParameter;

/**
 *
 * @author urs
 */
public class InterfaceGenerator extends Generator<Interface> {

  public InterfaceGenerator(ElementInfo info, String name, Collection<TemplateParameter> param, Interface item) {
    super(info, name, param, item);
  }
}
