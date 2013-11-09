package fun.expression.reference;

import java.util.LinkedList;

import common.ElementInfo;

import fun.expression.Expression;
import fun.other.Named;

final public class Reference extends Expression {
  private Named link;
  final private LinkedList<RefItem> offset = new LinkedList<RefItem>();

  public Reference(ElementInfo info, Named link) {
    super(info);
    this.link = link;
  }

  public Reference(ElementInfo info, String link) {
    super(info);
    this.link = new DummyLinkTarget(info, link);
  }

  public Named getLink() {
    return link;
  }

  public void setLink(Named link) {
    this.link = link;
  }

  public LinkedList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    String ret = link.getName();
    for (RefItem item : offset) {
      ret += item.toString();
    }
    return ret;
  }

}
