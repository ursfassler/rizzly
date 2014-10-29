package cir.function;

import java.util.List;

import cir.CirBase;
import cir.expression.reference.Referencable;
import cir.other.Named;
import cir.type.TypeRef;
import cir.variable.FuncVariable;

abstract public class Function extends CirBase implements Named, Referencable {
  private String name;
  private TypeRef retType;
  final private List<FuncVariable> argument;

  public Function(String name, TypeRef retType, List<FuncVariable> argument) {
    super();
    this.name = name;
    this.retType = retType;
    this.argument = argument;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TypeRef getRetType() {
    return retType;
  }

  public void setRetType(TypeRef retType) {
    this.retType = retType;
  }

  public List<FuncVariable> getArgument() {
    return argument;
  }

}
