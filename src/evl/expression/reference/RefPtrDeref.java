package evl.expression.reference;

import common.ElementInfo;

/**
 *
 * @author urs
 */
public class RefPtrDeref extends RefItem {

  public RefPtrDeref(ElementInfo info) {
    super(info);
  }

  @Override
  public String toString() {
    return "^";
  }
}
