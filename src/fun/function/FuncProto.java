package fun.function;

import common.ElementInfo;

import fun.content.ElementaryContent;
import fun.content.FileContent;
import fun.expression.reference.Reference;
import fun.hfsm.StateContent;
import fun.other.FunList;
import fun.variable.FuncVariable;

abstract public class FuncProto extends FuncHeader implements FileContent, ElementaryContent, StateContent {
  public FuncProto(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret) {
    super(info, name, param, ret);
  }
}
