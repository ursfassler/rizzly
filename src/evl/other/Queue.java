package evl.other;

import common.ElementInfo;

import evl.EvlBase;

public class Queue extends EvlBase implements Named {
  public static final String DEFAULT_NAME = "queue";
  private String name;

  public Queue(String name) {
    super(new ElementInfo());
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

}
