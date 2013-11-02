package pir.expression.reference;

import java.util.ArrayList;
import java.util.List;

import pir.expression.Expression;

final public class RefCall extends RefItem {
  final private List<Expression> parameter = new ArrayList<Expression>();

  public List<Expression> getParameter() {
    return parameter;
  }

  @Override
  public String toString() {
    return "(" + parameter + ")";
  }

}
