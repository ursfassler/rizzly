package cir.function;

import java.util.List;
import java.util.Set;

import cir.other.FuncVariable;
import cir.type.Type;

import common.FuncAttr;

public class LibFunction extends Function {

  public LibFunction(String name, Type retType, List<FuncVariable> argument, Set<FuncAttr> attributes) {
    super(name, retType, argument, attributes);
  }
}
