package fun.other;

import common.ElementInfo;

import fun.expression.reference.Reference;
import fun.function.FunctionHeader;
import fun.variable.Constant;
import fun.variable.Variable;

public class ImplElementary extends Component {
  final private ListOfNamed<Variable> variable = new ListOfNamed<Variable>();
  final private ListOfNamed<Constant> constant = new ListOfNamed<Constant>();
  final private ListOfNamed<FunctionHeader> function = new ListOfNamed<FunctionHeader>();
  private Reference entryFunc = null;
  private Reference exitFunc = null;

  public ImplElementary(ElementInfo info, String name) {
    super(info, name);
  }

  public ListOfNamed<FunctionHeader> getFunction() {
    return function;
  }

  public ListOfNamed<Variable> getVariable() {
    return variable;
  }

  public ListOfNamed<Constant> getConstant() {
    return constant;
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
