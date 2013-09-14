package pir.statement.normal;

import pir.other.SsaVariable;
import pir.type.PointerType;
import pir.type.Type;

/**
 *
 * @author urs
 */
public class StackMemoryAlloc extends NormalStmt implements VariableGeneratorStmt {
  private SsaVariable variable;

  public StackMemoryAlloc(SsaVariable addr) {
    this.variable = addr;
    assert ( addr.getType().getRef() instanceof PointerType );
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  @Override
  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }

  public Type getType() {
    Type varType = getVariable().getType().getRef();
    assert ( varType instanceof PointerType );
    return ( (PointerType) varType ).getType().getRef();
  }
}
