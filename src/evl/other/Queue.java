package evl.other;

import common.Designator;
import common.ElementInfo;

import evl.EvlBase;

public class Queue extends EvlBase implements Named {
  public static final String DEFAULT_NAME = Designator.NAME_SEP + "queue";
  private String name = DEFAULT_NAME;

  public Queue() {
    super(ElementInfo.NO);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
