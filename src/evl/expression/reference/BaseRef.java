package evl.expression.reference;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.Named;

abstract public class BaseRef<T extends Named> extends Expression {
  private T link;

  public BaseRef(ElementInfo info, T link) {
    super(info);
    this.link = link;
  }

  public T getLink() {
    return link;
  }

  public void setLink(T link) {
    this.link = link;
  }

  @Override
  public String toString() {
    return "->" + link;
  }
}
