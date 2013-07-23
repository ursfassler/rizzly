package fun.expression.reference;

import java.util.List;

import common.ElementInfo;

import fun.expression.Expression;

final public class RefCompcall extends RefItem {
  final private List<Expression> actualParameter;

  public RefCompcall(ElementInfo info, List<Expression> expr) {
    super(info);
    this.actualParameter = expr;
  }

  public List<Expression> getActualParameter() {
    return actualParameter;
  }

  @Override
  public String toString() {
    String ret = "";
    ret += "{";
    boolean first = true;
    for (Expression gen : actualParameter) {
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
