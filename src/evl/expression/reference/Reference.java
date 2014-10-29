package evl.expression.reference;

import common.ElementInfo;

import evl.other.EvlList;
import evl.other.Named;

final public class Reference extends BaseRef<Named> {
  final private EvlList<RefItem> offset;

  public Reference(ElementInfo info, Named link, EvlList<RefItem> offset) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>(offset);
  }

  public Reference(ElementInfo info, Named link, RefItem itm) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>();
    this.offset.add(itm);
  }

  public Reference(ElementInfo info, Named link) {
    super(info, link);
    assert (link != null);
    this.offset = new EvlList<RefItem>();
  }

  public EvlList<RefItem> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    String ret = super.toString();
    for (RefItem item : offset) {
      ret += item.toString();
    }
    return ret;
  }

}
