package fun.expression.reference;

import common.ElementInfo;

import fun.other.ActualTemplateArgument;
import fun.other.FunList;

final public class RefTemplCall extends RefItem {
  final private FunList<ActualTemplateArgument> actualParameter;

  public RefTemplCall(ElementInfo info, FunList<ActualTemplateArgument> expr) {
    super(info);
    this.actualParameter = expr;
  }

  public FunList<ActualTemplateArgument> getActualParameter() {
    return actualParameter;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += "{";
    boolean first = true;
    for (ActualTemplateArgument gen : actualParameter) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += gen.toString();
    }
    ret += "}";
    return ret;
  }

}
