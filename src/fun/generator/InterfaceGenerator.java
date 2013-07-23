package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.other.Interface;
import fun.variable.CompfuncParameter;

/**
 *
 * @author urs
 */
public class InterfaceGenerator extends Generator<Interface> {

  public InterfaceGenerator(ElementInfo info, String name, Collection<CompfuncParameter> param, Interface item) {
    super(info, name, param, item);
  }
}
