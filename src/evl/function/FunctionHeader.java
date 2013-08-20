package evl.function;

import evl.other.ListOfNamed;
import evl.other.Named;
import evl.variable.Variable;

public interface FunctionHeader extends Named {
  public ListOfNamed<Variable> getParam();

}
