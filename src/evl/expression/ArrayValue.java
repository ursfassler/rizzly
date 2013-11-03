package evl.expression;

import java.util.List;

import common.ElementInfo;

public class ArrayValue extends Expression {
  final private List<Expression> value;

  public ArrayValue(ElementInfo info, List<Expression> value) {
    super(info);
    this.value = value;
  }

  public List<Expression> getValue() {
    return value;
  }

}
