package evl.other;

import common.ElementInfo;

import evl.EvlBase;
import evl.function.FunctionBase;

final public class Interface extends EvlBase implements Named {
  private String name;
  final private ListOfNamed<FunctionBase> prototype = new ListOfNamed<FunctionBase>();

  public Interface(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public ListOfNamed<FunctionBase> getPrototype() {
    return prototype;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
