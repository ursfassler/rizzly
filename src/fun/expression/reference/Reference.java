package fun.expression.reference;

import common.ElementInfo;

import fun.other.FunList;
import fun.other.Named;

final public class Reference extends BaseRef {
  final private FunList<RefItem> offset = new FunList<RefItem>();

  public Reference(ElementInfo info, Named link) {
    super(info, link);
  }

  public Reference(ElementInfo info, String name) {
    super(info, new DummyLinkTarget(info, name));
  }

  public FunList<RefItem> getOffset() {
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
