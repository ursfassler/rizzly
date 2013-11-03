package evl.function.impl;

import common.ElementInfo;

import evl.function.FuncIfaceIn;
import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.FuncVariable;

public class FuncIfaceInVoid extends FunctionBase implements FuncIfaceIn {

  public FuncIfaceInVoid(ElementInfo info, String name, ListOfNamed<FuncVariable> param) {
    super(info, name, param);
  }

}
