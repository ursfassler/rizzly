package fun.expression.reference;

import java.util.List;

import common.ElementInfo;

import fun.expression.Expression;

final public class RefCall extends RefItem {
  private List<Expression> actualParameter;

  public RefCall(ElementInfo info, List<Expression> actualParameter) {
    super(info);
    this.actualParameter = actualParameter;
  }

  public List<Expression> getActualParameter() {
    return actualParameter;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += "(";
    boolean first = true;
    for (Expression gen : actualParameter) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += gen.toString();
    }
    ret += ")";
    return ret;
  }

}
