package evl.other;

import common.ElementInfo;

import evl.expression.reference.Reference;
import evl.function.FunctionHeader;
import evl.variable.Constant;
import evl.variable.Variable;

public class ImplElementary extends Component {
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private ListOfNamed<FunctionHeader> function = new ListOfNamed<FunctionHeader>();
  final private ListOfNamed<SubCallbacks> subComCallback = new ListOfNamed<SubCallbacks>();
  private Reference entryFunc = null;
  private Reference exitFunc = null;

  public ImplElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public void addSubCallback(String namespace, FunctionHeader prot) {
    SubCallbacks list = subComCallback.find(namespace);
    if (list == null) {
      list = new SubCallbacks(getInfo(), namespace);
      subComCallback.add(list);
    }
    list.add(prot);
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public ListOfNamed<FunctionHeader> getFunction() {
    return function;
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
  }

  public ListOfNamed<SubCallbacks> getSubCallback() {
    return subComCallback;
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

}
