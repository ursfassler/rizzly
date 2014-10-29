package evl.expression;

import common.ElementInfo;

import evl.other.EvlList;

public class ExprList extends Expression {
  final private EvlList<Expression> value;

  public ExprList(ElementInfo info, EvlList<Expression> value) {
    super(info);
    this.value = value;
  }

  public EvlList<Expression> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
