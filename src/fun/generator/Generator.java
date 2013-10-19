package fun.generator;

import java.util.List;

import common.ElementInfo;

import fun.FunBase;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.TemplateParameter;

/**
 * 
 * @author urs
 */
abstract public class Generator extends FunBase implements Named {
  private String name;
  final private ListOfNamed<TemplateParameter> param;

  public Generator(ElementInfo info, String name, List<TemplateParameter> param) {
    super(info);
    this.name = name;
    this.param = new ListOfNamed<TemplateParameter>(param);
  }

  public ListOfNamed<TemplateParameter> getParam() {
    return param;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + param;
  }

}
