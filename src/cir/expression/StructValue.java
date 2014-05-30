package cir.expression;

import java.util.ArrayList;
import java.util.List;

public class StructValue extends Expression {
  final private List<ElementValue> value = new ArrayList<ElementValue>();

  public List<ElementValue> getValue() {
    return value;
  }

}
