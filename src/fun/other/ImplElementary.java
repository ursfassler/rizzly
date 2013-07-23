package fun.other;

import java.util.List;

import common.ElementInfo;

import error.ErrorType;
import error.RError;
import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.variable.CompUse;
import fun.variable.Constant;
import fun.variable.Variable;

public class ImplElementary extends Component {
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<CompUse> component = new ListOfNamed<CompUse>();
  final private Namespace function = new Namespace(new ElementInfo(), "!function");
  private Reference entryFunc = null;
  private Reference exitFunc = null;

  public ImplElementary(ElementInfo info) {
    super(info);
  }

  public void addFunction(List<String> namespace, FunctionHeader func) {
    Namespace parent = function;
    for (String itr : namespace) {
      Named sn = parent.find(itr);
      if (sn == null) {
        sn = new Namespace(new ElementInfo(), itr);
        parent.add(sn);
      } else if (sn instanceof Namespace) {
      } else {
        RError.err(ErrorType.Error, func.getInfo(), "Name collission: " + namespace + " <-> " + sn);
      }
      parent = (Namespace) sn;
    }
    parent.add(func);
  }

  public Namespace getFunction() {
    return function;
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
  }

  public ListOfNamed<CompUse> getComponent() {
    return component;
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
