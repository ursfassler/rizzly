package evl.function.header;

import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.function.InterfaceFunction;
import evl.other.EvlList;
import evl.statement.Block;
import evl.type.Type;
import evl.variable.FuncVariable;

public class FuncCtrlOutDataOut extends Function implements InterfaceFunction {
  public FuncCtrlOutDataOut(ElementInfo info, String name, EvlList<FuncVariable> param, SimpleRef<Type> ret, Block body) {
    super(info, name, param, ret, body);
  }

}
