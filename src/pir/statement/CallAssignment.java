package pir.statement;

import java.util.ArrayList;

import pir.expression.reference.Reference;
import pir.function.Function;
import pir.other.PirValue;
import pir.other.SsaVariable;

final public class CallAssignment extends VariableGeneratorStmt implements Reference<Function> {
  private Function ref;
  private ArrayList<PirValue> parameter;

  public CallAssignment(SsaVariable variable, Function ref, ArrayList<PirValue> parameter) {
    super(variable);
    this.ref = ref;
    this.parameter = parameter;
  }

  @Override
  public Function getRef() {
    return ref;
  }

  @Override
  public void setRef(Function ref) {
    this.ref = ref;
  }

  public ArrayList<PirValue> getParameter() {
    return parameter;
  }

  @Override
  public String toString() {
    return getVariable() + " := call " + ref.toString() + parameter;
  }

}
