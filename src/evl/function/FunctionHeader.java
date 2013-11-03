package evl.function;

import java.util.Set;

import common.FuncAttr;

import evl.other.ListOfNamed;
import evl.other.Named;
import evl.variable.FuncVariable;

public interface FunctionHeader extends Named {
  public ListOfNamed<FuncVariable> getParam();

  public void setAttribute(FuncAttr attr); // XXX are they arguments of the named interface?

  public Set<FuncAttr> getAttributes(); // XXX are they arguments of the named interface?
}
