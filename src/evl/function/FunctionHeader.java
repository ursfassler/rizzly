package evl.function;

import evl.other.ListOfNamed;
import evl.other.Named;
import evl.variable.FuncVariable;

public interface FunctionHeader extends Named {
  public ListOfNamed<FuncVariable> getParam();

}
