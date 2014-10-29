package evl.composition;

import common.ElementInfo;

import evl.expression.reference.BaseRef;
import evl.function.Function;
import evl.other.Named;

abstract public class Endpoint<T extends Named> extends BaseRef<T> {
  public Endpoint(ElementInfo info, T link) {
    super(info, link);
  }

  abstract public Function getFunc();
}
