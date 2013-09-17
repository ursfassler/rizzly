package fun.type.composed;

import common.ElementInfo;
import fun.FunBase;
import fun.other.Named;

/**
 *
 * @author urs
 */
public class UnionSelector extends FunBase implements Named {
  private String name;

  public UnionSelector(ElementInfo info, String name) {
    super(info);
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

  @Override
  public String toString() {
    return name;
  }

}
