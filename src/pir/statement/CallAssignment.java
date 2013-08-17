package pir.statement;

import java.util.ArrayList;

import pir.expression.PExpression;
import pir.expression.reference.Reference;
import pir.function.Function;
import pir.other.Variable;

final public class CallAssignment extends VariableGeneratorStmt implements Reference<Function> {
  private Function ref;
  private ArrayList<PExpression> parameter;

  public CallAssignment(Variable variable, Function ref, ArrayList<PExpression> parameter) {
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

  public ArrayList<PExpression> getParameter() {
    return parameter;
  }

  @Override
  public String toString() {
    return ref.toString() + parameter;
  }

}
