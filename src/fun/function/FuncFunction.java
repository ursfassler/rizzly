package fun.function;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

public class FuncFunction extends FuncImpl {

  public FuncFunction(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret, Block body) {
    super(info, name, param, ret, body);
  }

}
