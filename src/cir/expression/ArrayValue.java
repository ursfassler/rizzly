package cir.expression;

import java.util.ArrayList;
import java.util.List;

public class ArrayValue extends Expression {
  final private List<Expression> value = new ArrayList<Expression>();

  public List<Expression> getValue() {
    return value;
  }

}
