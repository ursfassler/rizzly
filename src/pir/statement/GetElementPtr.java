package pir.statement;

import java.util.ArrayList;
import java.util.List;

import pir.expression.PExpression;
import pir.other.Variable;

public class GetElementPtr extends VariableGeneratorStmt {
  private Variable base;
  final private List<PExpression> offset = new ArrayList<PExpression>();

  public GetElementPtr(Variable variable, Variable base, List<PExpression> offset) {
    super(variable);
    this.base = base;
    this.offset.addAll(offset);
  }

  public Variable getBase() {
    return base;
  }

  public void setBase(Variable base) {
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
