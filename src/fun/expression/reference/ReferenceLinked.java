package fun.expression.reference;

import common.ElementInfo;

import fun.other.Named;

final public class ReferenceLinked extends Reference {
  private Named link;

  public ReferenceLinked(ElementInfo info, Named link) {
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
    String ret = link.getName() + super.toString();
    return ret;
  }

}
