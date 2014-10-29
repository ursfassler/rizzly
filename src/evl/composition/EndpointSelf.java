package evl.composition;

import common.ElementInfo;

import evl.function.Function;

final public class EndpointSelf extends Endpoint<Function> {

  public EndpointSelf(ElementInfo info, Function link) {
    super(info, link);
  }

  @Override
  public Function getFunc() {
    return getLink();
  }

  @Override
  public String toString() {
    return "self." + getLink().getName();
  }
}
