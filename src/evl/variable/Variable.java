package evl.variable;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.SimpleRef;
import evl.other.Named;
import evl.type.Type;

abstract public class Variable extends EvlBase implements Named {
  private String name;
  private SimpleRef<Type> type;

  public Variable(ElementInfo info, String name, SimpleRef<Type> type) {
    super(info);
    this.type = type;
    this.name = name;
  }

  public SimpleRef<Type> getType() {
    return type;
  }

  public void setType(SimpleRef<Type> type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name + ":" + type.getLink();
  }

}
