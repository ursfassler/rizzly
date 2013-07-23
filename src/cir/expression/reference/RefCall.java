package cir.expression.reference;

import java.util.ArrayList;
import java.util.List;

import cir.expression.Expression;


final public class RefCall extends RefMiddle {
  final private List<Expression> parameter = new ArrayList<Expression>();

  public RefCall(RefItem previous) {
    super(previous);
  }

  public List<Expression> getParameter() {
    return parameter;
  }

  @Override
  public String toString() {
    return "(" + parameter + ")";
  }

}
