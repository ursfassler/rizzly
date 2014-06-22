package fun.other;

import common.ElementInfo;

import fun.FunBase;
import fun.function.FunctionHeader;
import fun.function.impl.FuncProtQuery;
import fun.function.impl.FuncProtResponse;
import fun.function.impl.FuncProtSignal;
import fun.function.impl.FuncProtSlot;
import fun.variable.TemplateParameter;

abstract public class Component extends FunBase implements Generator, Named {
  final private ListOfNamed<TemplateParameter> param = new ListOfNamed<TemplateParameter>();
  final private ListOfNamed<FuncProtResponse> response = new ListOfNamed<FuncProtResponse>();
  final private ListOfNamed<FuncProtQuery> query = new ListOfNamed<FuncProtQuery>();
  final private ListOfNamed<FuncProtSignal> signal = new ListOfNamed<FuncProtSignal>();
  final private ListOfNamed<FuncProtSlot> slot = new ListOfNamed<FuncProtSlot>();
  private String name;

  public Component(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ListOfNamed<FuncProtResponse> getResponse() {
    return response;
  }

  public ListOfNamed<FuncProtQuery> getQuery() {
    return query;
  }

  public ListOfNamed<FuncProtSignal> getSignal() {
    return signal;
  }

  public ListOfNamed<FuncProtSlot> getSlot() {
    return slot;
  }

  @Override
  public ListOfNamed<TemplateParameter> getTemplateParam() {
    return param;
  }

  @Override
  public String toString() {
    String ret = name;
    if (!param.isEmpty()) {
      ret += "{";
      boolean first = true;
      for (TemplateParameter tp : param) {
        if (first) {
          first = false;
        } else {
          ret += "; ";
        }
        ret += tp.toString();
      }
      ret += "}";
    }
    return ret;
  }

  public ListOfNamed<? extends FunctionHeader> getIface() {
    ListOfNamed<FunctionHeader> ret = new ListOfNamed<FunctionHeader>();
    ret.addAll(response);
    ret.addAll(query);
    ret.addAll(signal);
    ret.addAll(slot);
    return ret;
  }

}
