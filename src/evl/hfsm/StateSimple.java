package evl.hfsm;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.function.header.FuncPrivateVoid;

final public class StateSimple extends State {

  public StateSimple(ElementInfo info, String name, SimpleRef<FuncPrivateVoid> entryFunc, SimpleRef<FuncPrivateVoid> exitFunc) {
    super(info, name, entryFunc, exitFunc);
  }

}
