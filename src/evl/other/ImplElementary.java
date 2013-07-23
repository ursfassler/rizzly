package evl.other;

import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import evl.expression.reference.Reference;
import evl.function.FunctionBase;
import evl.variable.Constant;
import evl.variable.Variable;

public class ImplElementary extends Component {
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private ListOfNamed<FunctionBase> function = new ListOfNamed<FunctionBase>();
  final private ListOfNamed<NamedList<FunctionBase>> inFunc = new ListOfNamed<NamedList<FunctionBase>>();
  final private ListOfNamed<NamedList<NamedList<FunctionBase>>> subComCallback = new ListOfNamed<NamedList<NamedList<FunctionBase>>>();
  private Reference entryFunc = null;
  private Reference exitFunc = null;

  public ImplElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public void addFunction(List<String> namespace, FunctionBase prot) {
    switch (namespace.size()) {
    case 0: {
      function.add(prot);
      break;
    }
    case 1: {
      NamedList<FunctionBase> list = inFunc.find(namespace.get(0));
      if (list == null) {
        list = new NamedList<FunctionBase>(getInfo(), namespace.get(0));
        inFunc.add(list);
      }
      list.add(prot);
      break;
    }
    case 2: {
      NamedList<NamedList<FunctionBase>> compList = subComCallback.find(namespace.get(0));
      if (compList == null) {
        compList = new NamedList<NamedList<FunctionBase>>(getInfo(), namespace.get(0));
        subComCallback.add(compList);
      }
      NamedList<FunctionBase> funcList = compList.find(namespace.get(1));
      if (funcList == null) {
        funcList = new NamedList<FunctionBase>(getInfo(), namespace.get(1));
        compList.add(funcList);
      }
      funcList.add(prot);
      break;
    }
    default: {
      RError.err(ErrorType.Error, prot.getInfo(), "Namespace can have max deepth of 2, got: " + namespace);
    }
    }
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public ListOfNamed<FunctionBase> getInternalFunction() {
    return function;
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
  }

  public ListOfNamed<NamedList<FunctionBase>> getInputFunc() {
    return inFunc;
  }

  public ListOfNamed<NamedList<NamedList<FunctionBase>>> getSubComCallback() {
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
