package evl.expression.reference;

import common.ElementInfo;

import evl.other.Named;

final public class SimpleRef<T extends Named> extends BaseRef<T> {

  public SimpleRef(ElementInfo info, T link) {
    super(info, link);
  }
}
