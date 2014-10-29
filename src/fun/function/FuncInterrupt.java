package fun.function;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

/**
 * Function inside a component. It may be not pure and can therefore not be executed at compile time.
 * 
 * @author urs
 */
final public class FuncInterrupt extends FuncImpl {

  public FuncInterrupt(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret, Block body) {
    super(info, name, param, ret, body);
  }

}
