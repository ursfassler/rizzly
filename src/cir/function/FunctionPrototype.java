package cir.function;

import java.util.List;

import cir.type.TypeRef;
import cir.variable.FuncVariable;

final public class FunctionPrototype extends Function {

  public FunctionPrototype(String name, TypeRef retType, List<FuncVariable> argument) {
    super(name, retType, argument);
  }
}
