package evl.expression.reference;

import java.util.Collection;
import java.util.LinkedList;

import common.ElementInfo;

import evl.expression.Expression;
import evl.other.Named;


public class Reference extends Expression {
  final private LinkedList<RefItem> offset;
  private Named link;

  public Reference(ElementInfo info, Named link, Collection<RefItem> offset) {
    super(info);
    assert(link != null);
    this.link = link;
    this.offset = new LinkedList<RefItem>(offset);
  }

  public Reference(ElementInfo info, Named link) {
    super(info);
    assert(link != null);
    this.link = link;
    this.offset = new LinkedList<RefItem>();
  }

  public LinkedList<RefItem> getOffset() {
    return offset;
  }

  public Named getLink() {
    return link;
  }

  public void setLink(Named link) {
    assert(link != null);
    this.link = link;
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
