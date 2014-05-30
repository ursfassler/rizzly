package fun.expression;

import java.util.List;

import common.ElementInfo;

public class ExprList extends Expression {
  final private List<Expression> value;

  public ExprList(ElementInfo info, List<Expression> value) {
    super(info);
    this.value = value;
  }

  public List<Expression> getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

}
