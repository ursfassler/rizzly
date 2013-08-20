package evl.function.impl;

import common.ElementInfo;

import evl.function.FunctionBase;
import evl.other.ListOfNamed;
import evl.variable.Variable;

/**
 *
 * @author urs
 */
public class FuncProtoVoid extends FunctionBase {

  public FuncProtoVoid(ElementInfo info, String name, ListOfNamed<Variable> param) {
    super(info, name, param);
  }

}
