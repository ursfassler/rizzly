package evl.other;

import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.expression.reference.Reference;
import evl.function.FunctionHeader;
import evl.variable.Constant;
import evl.variable.Variable;

public class ImplElementary extends Component {
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private ListOfNamed<FunctionHeader> function = new ListOfNamed<FunctionHeader>();
  final private ListOfNamed<NamedList<FunctionHeader>> subComCallback = new ListOfNamed<NamedList<FunctionHeader>>();
  private Reference entryFunc = null;
  private Reference exitFunc = null;

  public ImplElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public void addFunction(List<String> namespace, FunctionHeader prot) {
    switch (namespace.size()) {
    case 0: {
      function.add(prot);
      break;
    }
    case 1: {
      NamedList<FunctionHeader> list = subComCallback.find(namespace.get(0));
      if (list == null) {
        list = new NamedList<FunctionHeader>(getInfo(), namespace.get(0));
        subComCallback.add(list);
      }
      list.add(prot);
      break;
    }
    default: {
      RError.err(ErrorType.Error, prot.getInfo(), "Namespace can have max deepth of 1, got: " + namespace);
    }
    }
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public ListOfNamed<FunctionHeader> getInternalFunction() {
    return function;
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
  }

  public ListOfNamed<NamedList<FunctionHeader>> getSubComCallback() {
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
