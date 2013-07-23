package evl.other;

import common.ElementInfo;

import evl.EvlBase;

final public class IfaceUse extends EvlBase implements Named {

  private String name;
  private Interface link;

  public IfaceUse(ElementInfo info, String name, Interface type) {
    super(info);
    assert (name != null);
    this.name = name;
    this.link = type;
  }

  public Interface getLink() {
    return link;
  }

  public void setLink(Interface type) {
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
