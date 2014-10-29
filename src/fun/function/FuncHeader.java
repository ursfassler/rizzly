package fun.function;

import common.ElementInfo;

import fun.Fun;
import fun.FunBase;
import fun.expression.reference.Reference;
import fun.other.FunList;
import fun.other.Named;
import fun.variable.FuncVariable;

abstract public class FuncHeader extends FunBase implements Named {
  private String name;
  final private FunList<FuncVariable> param = new FunList<FuncVariable>();
  private Reference ret;

  public FuncHeader(ElementInfo info, String name, FunList<FuncVariable> param, Reference ret) {
    super(info);
    this.name = name;
    this.param.addAll(param);
    this.ret = ret;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public FunList<FuncVariable> getParam() {
    return param;
  }

  public Reference getRet() {
    return ret;
  }

  public void setRet(Reference ret) {
    this.ret = ret;
  }

  @Override
  public String toString() {
    String ret = name;

    ret += "(";
    ret += l2s(param);
    ret += ")";

    return ret;
  }

  private String l2s(FunList<? extends Fun> list) {
    String ret = "";
    boolean first = true;
    for (Fun tp : list) {
      if (first) {
        first = false;
      } else {
        ret += "; ";
      }
      ret += tp.toString();
    }
    return ret;
  }

}
