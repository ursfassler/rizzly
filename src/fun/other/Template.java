package fun.other;

import java.util.List;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.hfsm.StateContent;
import fun.variable.TemplateParameter;

//TODO do we need anonymous templates? it is easier if they are named.
public class Template extends FunBase implements Named, StateContent {
  private String name;
  private final FunList<TemplateParameter> templ = new FunList<TemplateParameter>();
  private final Fun object;

  public Template(ElementInfo info, String name, List<TemplateParameter> genpam, Fun object) {
    super(info);
    this.name = name;
    this.templ.addAll(genpam);
    this.object = object;
  }

  public FunList<TemplateParameter> getTempl() {
    return templ;
  }

  public Fun getObject() {
    return object;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + templ.toString();
  }

}
