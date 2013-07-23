package cir.function;

import java.util.List;
import java.util.Set;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.other.FuncVariable;
import cir.type.Type;

import common.FuncAttr;

abstract public class Function extends CirBase implements Referencable {
  private String name;
  private Type retType;
  final private List<FuncVariable> argument;
  final private Set<FuncAttr> attributes;

  public Function(String name, Type retType, List<FuncVariable> argument, Set<FuncAttr> attributes) {
    super();
    this.name = name;
    this.retType = retType;
    this.argument = argument;
    this.attributes = attributes;
  }

  public String getName() {
    return name;
  }

  public Type getRetType() {
    return retType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRetType(Type retType) {
    this.retType = retType;
  }

  public List<FuncVariable> getArgument() {
    return argument;
  }

  public Set<FuncAttr> getAttributes() {
    return attributes;
  }

}
