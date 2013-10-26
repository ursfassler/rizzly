package fun.other;

import java.util.List;

import common.ElementInfo;

import fun.FunBase;
import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
final public class Generator extends FunBase implements Named {
  final private ListOfNamed<TemplateParameter> param;
  final private Named template;

  public Generator(ElementInfo info, Named template, List<TemplateParameter> param) {
    super(info);
    this.template = template;
    this.param = new ListOfNamed<TemplateParameter>(param);
  }

  public ListOfNamed<TemplateParameter> getParam() {
    return param;
  }

  public String getName() {
    return template.getName();
  }

  public Named getTemplate() {
    return template;
  }

  @Override
  public String toString() {
    return template.getName() + param;
  }

  @Override
  public void setName(String name) {
    throw new RuntimeException("not yet implemented");
  }

}
