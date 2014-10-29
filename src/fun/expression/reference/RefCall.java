package fun.expression.reference;

import common.ElementInfo;

import fun.expression.Expression;
import fun.other.FunList;

final public class RefCall extends RefItem {
  private FunList<Expression> actualParameter;

  public RefCall(ElementInfo info, FunList<Expression> actualParameter) {
    super(info);
    this.actualParameter = actualParameter;
  }

  public FunList<Expression> getActualParameter() {
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
