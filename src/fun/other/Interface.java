package fun.other;

import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionHeader;

final public class Interface extends FunBase implements Named {
  final private ListOfNamed<FunctionHeader> prototype = new ListOfNamed<FunctionHeader>();
  private String name;

  public Interface(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public ListOfNamed<FunctionHeader> getPrototype() {
    return prototype;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
