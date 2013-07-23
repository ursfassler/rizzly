package fun.other;

import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionHeader;

final public class Interface extends FunBase {
  final private ListOfNamed<FunctionHeader> prototype = new ListOfNamed<FunctionHeader>();

  public Interface(ElementInfo info) {
    super(info);
  }

  public ListOfNamed<FunctionHeader> getPrototype() {
    return prototype;
  }

}
