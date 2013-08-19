package pir.statement;

import java.util.ArrayList;
import java.util.List;

import pir.expression.PExpression;
import pir.expression.reference.VarRef;
import pir.other.Variable;

public class GetElementPtr extends VariableGeneratorStmt {
  private VarRef base;
  final private List<PExpression> offset = new ArrayList<PExpression>();

  public GetElementPtr(Variable variable, VarRef base, List<PExpression> offset) {
    super(variable);
    this.base = base;
    this.offset.addAll(offset);
  }

  public VarRef getBase() {
    return base;
  }

  public void setBase(VarRef base) {
    this.base = base;
  }

  public List<PExpression> getOffset() {
    return offset;
  }

  @Override
  public String toString() {
    return super.toString() + " := getelementptr " + base + offset;
  }

}
