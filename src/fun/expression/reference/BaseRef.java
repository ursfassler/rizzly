package fun.expression.reference;

import common.ElementInfo;

import fun.expression.Expression;
import fun.other.Named;

public class BaseRef extends Expression {
  private Named link;

  public BaseRef(ElementInfo info, Named link) {
    super(info);
    this.link = link;
  }

  public Named getLink() {
    return link;
  }

  public void setLink(Named link) {
    this.link = link;
  }

  @Override
  public String toString() {
    return link.getName();
  }

}
