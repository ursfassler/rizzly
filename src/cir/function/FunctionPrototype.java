package cir.function;

import java.util.List;
import java.util.Set;

import cir.other.FuncVariable;
import cir.type.TypeRef;

import common.FuncAttr;

public class FunctionPrototype extends Function {

  public FunctionPrototype(String name, TypeRef retType, List<FuncVariable> argument, Set<FuncAttr> attributes) {
    super(name, retType, argument, attributes);
  }
}
