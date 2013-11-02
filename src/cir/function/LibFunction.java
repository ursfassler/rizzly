package cir.function;

import java.util.List;
import java.util.Set;

import cir.other.FuncVariable;
import cir.type.TypeRef;

import common.FuncAttr;

public class LibFunction extends Function {

  public LibFunction(String name, TypeRef retType, List<FuncVariable> argument, Set<FuncAttr> attributes) {
    super(name, retType, argument, attributes);
  }
}
