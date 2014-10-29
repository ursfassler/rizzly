package fun.expression;

import common.ElementInfo;

import fun.other.FunList;

public class ExprList extends Expression {
  final private FunList<Expression> value;

  public ExprList(ElementInfo info, FunList<Expression> value) {
    super(info);
    this.value = value;
  }

  public FunList<Expression> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
