package fun.function;

import common.ElementInfo;

import fun.FunBase;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.FuncVariable;

/**
 *
 * @author urs
 */
abstract public class FunctionHeader extends FunBase implements Named {
  private String name;
  private ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();

  public FunctionHeader(ElementInfo info) {
    super(info);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  public ListOfNamed<FuncVariable> getParam() {
    return param;
  }

  @Override
  public String toString() {
    return name + " " + param;
  }

}
