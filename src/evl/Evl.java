package evl;

import java.util.Map;

import common.ElementInfo;

/**
 * 
 * @author urs
 */
public interface Evl {
  public ElementInfo getInfo();

  public Map<Object, Object> properties();
}
