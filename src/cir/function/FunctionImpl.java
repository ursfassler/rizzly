package cir.function;

import java.util.List;

import cir.statement.Block;
import cir.type.TypeRef;
import cir.variable.FuncVariable;

abstract public class FunctionImpl extends Function {
  final private Block body;

  public FunctionImpl(String name, TypeRef retType, List<FuncVariable> argument, Block body) {
    super(name, retType, argument);
    this.body = body;
  }

  public Block getBody() {
    return body;
  }

}
