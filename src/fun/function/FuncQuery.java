package fun.function;

import common.ElementInfo;

import fun.content.CompIfaceContent;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.variable.FuncVariable;

/**
 * 
 * @author urs
 */
public class FuncQuery extends FuncProto implements CompIfaceContent {

  public FuncQuery(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret) {
    super(info, name, param, ret);
  }

}
