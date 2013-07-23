package pir.expression;

import java.util.ArrayList;
import java.util.List;

public class ArrayValue extends PExpression {
  final private List<PExpression> value = new ArrayList<PExpression>();

  public List<PExpression> getValue() {
    return value;
  }

}
