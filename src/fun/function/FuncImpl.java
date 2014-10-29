package fun.function;

import common.ElementInfo;

import fun.content.ElementaryContent;
import fun.content.FileContent;
import fun.expression.reference.Reference;
import fun.hfsm.StateContent;
import fun.other.FunList;
import fun.statement.Block;
import fun.variable.FuncVariable;

abstract public class FuncImpl extends FuncHeader implements FileContent, ElementaryContent, StateContent {
  private Block body;

  public FuncImpl(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret, Block body) {
    super(info, name, param, ret);
    this.body = body;
  }

  public Block getBody() {
    return body;
  }

  public void setBody(Block body) {
    this.body = body;
  }

}
