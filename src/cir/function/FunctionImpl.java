package cir.function;

import java.util.List;
import java.util.Set;

import cir.other.FuncVariable;
import cir.statement.Block;
import cir.type.TypeRef;

import common.FuncAttr;

public class FunctionImpl extends Function {
  private Block body;

  public FunctionImpl(String name, TypeRef retType, List<FuncVariable> argument, Set<FuncAttr> attributes) {
    super(name, retType, argument, attributes);
  }

  public Block getBody() {
    return body;
  }

  public void setBody(Block body) {
    this.body = body;
  }

}
