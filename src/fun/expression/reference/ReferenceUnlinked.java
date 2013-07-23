package fun.expression.reference;

import common.Designator;
import common.ElementInfo;


final public class ReferenceUnlinked extends Reference {
  private Designator name;

  public ReferenceUnlinked(ElementInfo info, Designator name) {
    super(info);
    this.name = name;
  }

  public ReferenceUnlinked(ElementInfo info) {
    super(info);
    this.name = new Designator();
  }

  public Designator getName() {
    return name;
  }

  public void setName(Designator name) {
    this.name = name;
  }

  @Override
  public String toString() {
    String ret = name.toString() + super.toString();
    return ret;
  }

}
