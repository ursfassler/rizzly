package fun.hfsm;

import common.ElementInfo;

import fun.FunBase;
import fun.expression.reference.Reference;
import fun.other.ListOfNamed;
import fun.other.Named;
import fun.variable.Variable;

abstract public class State extends FunBase implements Named {
  public final static String TOPSTATE_NAME = "_top";
  public static final String ENTRY_FUNC_NAME = "_entry";
  public static final String EXIT_FUNC_NAME = "_exit";

  private String name;
  private Reference entryFuncRef = null;
  private Reference exitFuncRef = null;
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>(); // TODO also move into item
  final protected ListOfNamed<Named> item = new ListOfNamed<Named>();

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

  public ListOfNamed<Named> getItemList() {
    return item;
  }

  @Override
  public String toString() {
    return name;
  }

}
