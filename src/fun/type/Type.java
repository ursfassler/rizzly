package fun.type;

import common.ElementInfo;

import fun.FunBase;
import fun.other.ActualTemplateArgument;
import fun.other.Generator;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.TemplateParameter;

abstract public class Type extends FunBase implements Named, ActualTemplateArgument, Generator {
  final private ListOfNamed<TemplateParameter> param = new ListOfNamed<TemplateParameter>();
  private String name;

  public Type(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ListOfNamed<TemplateParameter> getTemplateParam() {
    return param;
  }

  @Override
  public String toString() {
    String ret = name;
    if (!param.isEmpty()) {
      ret += "{";
      boolean first = true;
      for (TemplateParameter tp : param) {
        if (first) {
          first = false;
        } else {
          ret += "; ";
        }
        ret += tp.toString();
      }
      ret += "}";
    }
    return ret;
  }

}
