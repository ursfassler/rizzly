package fun.type;

import common.ElementInfo;

import fun.other.Generator;
import fun.other.ListOfNamed;
import fun.variable.TemplateParameter;

public class TypeGenerator extends Type implements Generator {
  final private ListOfNamed<TemplateParameter> param = new ListOfNamed<TemplateParameter>();

  public TypeGenerator(ElementInfo info, String name) {
    super(info, name);
  }

  @Override
  public ListOfNamed<TemplateParameter> getTemplateParam() {
    return param;
  }

  @Override
  public String toString() {
    String ret = super.toString();
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
