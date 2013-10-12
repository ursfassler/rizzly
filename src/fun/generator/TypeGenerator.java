package fun.generator;

import java.util.Collection;

import common.ElementInfo;

import fun.type.Type;
import fun.variable.TemplateParameter;

/**
 *
 * @author urs
 */
public class TypeGenerator extends Generator<Type> {

  public TypeGenerator(ElementInfo info, String name, Collection<TemplateParameter> param, Type item) {
    super(info, name, param, item);
  }
}
