package pir.function;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pir.PirObject;
import pir.expression.reference.Referencable;
import pir.other.FuncVariable;

import common.FuncAttr;

abstract public class Function extends PirObject implements Referencable {
  private String name;
  final private List<FuncVariable> argument;
  final private Set<FuncAttr> attributes = new HashSet<FuncAttr>();

  public Function(String name, List<FuncVariable> argument) {
    super();
    this.name = name;
    this.argument = argument;
  }

  public String getName() {
    return name;
  }

  public List<FuncVariable> getArgument() {
    return argument;
  }

  public Set<FuncAttr> getAttributes() {
    return attributes;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    String ret = name;
    ret += "(";
    boolean first = true;
    for (FuncVariable var : argument) {
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
