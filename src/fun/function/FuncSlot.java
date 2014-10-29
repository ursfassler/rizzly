package fun.function;

import common.ElementInfo;

import fun.content.CompIfaceContent;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

/**
 * 
 * @author urs
 */
public class FuncSlot extends FuncImpl implements CompIfaceContent {

  public FuncSlot(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret, Block body) {
    super(info, name, param, ret, body);
  }

  public FuncSlot(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret) {
    super(info, name, param, ret, new Block(info));
  }

}
