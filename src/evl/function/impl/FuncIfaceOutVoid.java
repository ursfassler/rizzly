package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncIfaceOut;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

public class FuncIfaceOutVoid extends FunctionBase implements FuncIfaceOut {

  public FuncIfaceOutVoid(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

}
