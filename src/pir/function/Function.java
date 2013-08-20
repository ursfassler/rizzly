package pir.function;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.other.SsaVariable;
import pir.type.Type;

import common.FuncAttr;

abstract public class Function extends PirObject implements Referencable {
  private String name;
  final private List<SsaVariable> argument;
  private Type retType = null;
  final private Set<FuncAttr> attributes = new HashSet<FuncAttr>();

  public Function(String name, List<SsaVariable> argument, Type retType) {
    super();
    this.name = name;
    this.argument = argument;
    this.retType = retType;
  }

  public String getName() {
    return name;
  }

  public List<SsaVariable> getArgument() {
    return argument;
  }

  public Set<FuncAttr> getAttributes() {
    return attributes;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getRetType() {
    return retType;
  }

  public void setRetType(Type retType) {
    this.retType = retType;
  }

  @Override
  public String toString() {
    String ret = name;
    ret += "(";
    boolean first = true;
    for (SsaVariable var : argument) {
      if (first) {
        first = false;
      } else {
        ret += ",";
      }
      ret += var.toString();
    }
    ret += ")";
    return ret;
  }

}
