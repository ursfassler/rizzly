package fun.hfsm;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.Variable;

abstract public class State extends StateItem implements Named {
  public final static String TOPSTATE_NAME = "_top";
  public static final String ENTRY_FUNC_NAME = "_entry";
  public static final String EXIT_FUNC_NAME = "_exit";

  private String name;
  private Reference entryFuncRef = null;
  private Reference exitFuncRef = null;
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<FunctionHeader> bfunc = new ListOfNamed<FunctionHeader>();
  final protected List<StateItem> item = new ArrayList<StateItem>();

  public State(ElementInfo info, String name) {
    super(info);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<FunctionHeader> getBfunc() {
    return bfunc;
  }

  public Reference getEntryFuncRef() {
    return entryFuncRef;
  }

  public void setEntryFuncRef(Reference entryFuncRef) {
    this.entryFuncRef = entryFuncRef;
  }

  public Reference getExitFuncRef() {
    return exitFuncRef;
  }

  public void setExitFuncRef(Reference exitFuncRef) {
    this.exitFuncRef = exitFuncRef;
  }

  public List<StateItem> getItem() {
    return item;
  }

  public <T extends StateItem> List<T> getItemList(Class<T> kind) {
    List<T> ret = new ArrayList<T>();
    for (StateItem itr : item) {
      if (kind.isInstance(itr)) {
        ret.add((T) itr);
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    return name;
  }

}
