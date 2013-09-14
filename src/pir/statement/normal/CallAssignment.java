package pir.statement.normal;

import java.util.ArrayList;

import pir.expression.reference.Reference;
import pir.function.Function;
import pir.other.PirValue;
import pir.other.SsaVariable;

final public class CallAssignment extends NormalStmt implements VariableGeneratorStmt, Reference<Function> {
  private SsaVariable variable;
  private Function ref;
  private ArrayList<PirValue> parameter;

  public CallAssignment(SsaVariable variable, Function ref, ArrayList<PirValue> parameter) {
    this.variable = variable;
    this.ref = ref;
    this.parameter = parameter;
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
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
    return variable + " := call " + ref.toString() + parameter;
  }

}
