package cir.function;

import java.util.List;

import cir.statement.Block;
import cir.type.TypeRef;
import cir.variable.FuncVariable;

final public class FunctionPublic extends FunctionImpl {

  public FunctionPublic(String name, TypeRef retType, List<FuncVariable> argument, Block body) {
    super(name, retType, argument, body);
  }

}
