package evl.expression.reference;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.EvlList;

final public class RefCall extends RefItem {
  private EvlList<Expression> actualParameter;

  public RefCall(ElementInfo info, EvlList<Expression> actualParameter) {
    super(info);
    this.actualParameter = new EvlList<Expression>(actualParameter);
  }

  public EvlList<Expression> getActualParameter() {
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
