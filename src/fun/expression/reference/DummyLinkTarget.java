package fun.expression.reference;

import common.ElementInfo;

import fun.FunBase;
import fun.other.Named;

public class DummyLinkTarget extends FunBase implements Named {
  private String name;

  public DummyLinkTarget(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    throw new RuntimeException("not yet implemented");
  }

}
