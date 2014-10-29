package evl;

import java.util.HashMap;
import java.util.Map;

import common.ElementInfo;

public abstract class EvlBase implements Evl {
  final private HashMap<Object, Object> properties = new HashMap<Object, Object>();
  private ElementInfo info;

  public EvlBase(ElementInfo info) {
    super();
    this.info = info;
  }

  public ElementInfo getInfo() {
    return info;
  }

  @Override
  public Map<Object, Object> properties() {
    return properties;
  }

}
