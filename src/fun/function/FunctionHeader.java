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
  final private ListOfNamed<FuncVariable> param = new ListOfNamed<FuncVariable>();

  public FunctionHeader(ElementInfo info) {
    super(info);
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ListOfNamed<FuncVariable> getParam() {
    return param;
  }

  @Override
  public String toString() {
    String ret = name;
    ret += "(";
    boolean first = true;
    for (FuncVariable tp : param) {
      if (first) {
        first = false;
      } else {
        ret += "; ";
      }
      ret += tp.toString();
    }
    ret += ")";
    return ret;
  }

}
