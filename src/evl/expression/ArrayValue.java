package evl.expression;

import common.ElementInfo;

import evl.other.EvlList;

public class ArrayValue extends Expression {
  final private EvlList<Expression> value;

  public ArrayValue(ElementInfo info, EvlList<Expression> value) {
    super(info);
    this.value = value;
  }

  public EvlList<Expression> getValue() {
    return value;
  }

}
