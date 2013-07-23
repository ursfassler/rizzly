package evl.other;

import common.ElementInfo;

import evl.EvlBase;

final public class CompUse extends EvlBase implements Named {
  private String name;
  private Component link;

  public CompUse(ElementInfo info, String name, Component type) {
    super(info);
    assert (name != null);
    this.name = name;
    this.link = type;
  }

  public Component getLink() {
    return link;
  }

  public void setLink(Component type) {
    this.link = type;
  }

  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    assert (name != null);
    this.name = name;
  }

  @Override
  public String toString() {
    return name.toString() + ":" + link.toString();
  }

}
