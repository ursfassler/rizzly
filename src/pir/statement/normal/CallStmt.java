package pir.statement.normal;

import java.util.ArrayList;

import pir.expression.reference.Reference;
import pir.function.Function;
import pir.other.PirValue;

//TODO remove, use CallAssignment
final public class CallStmt extends NormalStmt implements Reference<Function> {
  private Function ref;
  private ArrayList<PirValue> parameter;

  public CallStmt(Function ref, ArrayList<PirValue> parameter) {
    super();
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
    return ref.toString() + parameter;
  }

}
