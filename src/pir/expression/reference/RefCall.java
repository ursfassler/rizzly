package pir.expression.reference;

import java.util.ArrayList;
import java.util.List;

import pir.expression.PExpression;


final public class RefCall extends RefMiddle {
  final private List<PExpression> parameter = new ArrayList<PExpression>();

  public RefCall(RefItem previous) {
    super(previous);
  }

  public List<PExpression> getParameter() {
    return parameter;
  }

  @Override
  public String toString() {
    return "(" + parameter + ")";
  }

}
