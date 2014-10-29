package evl.composition;

import common.ElementInfo;

import evl.function.Function;
import evl.other.CompUse;

final public class EndpointSub extends Endpoint<CompUse> {
  private String function;

  public EndpointSub(ElementInfo info, CompUse link, String function) {
    super(info, link);
    this.function = function;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }

  @Override
  public Function getFunc() {
    return (Function) getLink().getLink().getIface().find(function);
  }

  @Override
  public String toString() {
    return getLink().getName() + "." + function;
  }

}
