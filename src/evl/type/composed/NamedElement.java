package evl.type.composed;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.other.Named;
import evl.type.Type;

final public class NamedElement extends EvlBase implements Named {
  private String name;
  final private SimpleRef<Type> ref;

  public NamedElement(ElementInfo info, String name, SimpleRef<Type> ref) {
    super(info);
    this.name = name;
    this.ref = ref;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SimpleRef<Type> getRef() {
    return ref;
  }

  @Override
  public String toString() {
    return name + ref;
  }

}
