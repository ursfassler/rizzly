package evl.hfsm;

import java.util.ArrayList;
import java.util.List;

import common.ElementInfo;

import evl.EvlBase;
import evl.expression.reference.Reference;
import evl.function.FunctionHeader;
import evl.other.ListOfNamed;
import evl.variable.Variable;

abstract public class State extends EvlBase implements StateItem {
  public final static String TOPSTATE_NAME = "_top";

  private String name;
  private Reference entryFunc;
  private Reference exitFunc;
  final private ListOfNamed<FunctionHeader> function = new ListOfNamed<FunctionHeader>();
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
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

  public ListOfNamed<FunctionHeader> getFunction() {
    return function;
  }

  public Reference getEntryFunc() {
    assert (entryFunc != null);
    return entryFunc;
  }

  public void setEntryFunc(Reference entryFunc) {
    assert (entryFunc != null);
    this.entryFunc = entryFunc;
  }

  public Reference getExitFunc() {
    assert (exitFunc != null);
    return exitFunc;
  }

  public void setExitFunc(Reference exitFunc) {
    assert (exitFunc != null);
    this.exitFunc = exitFunc;
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
