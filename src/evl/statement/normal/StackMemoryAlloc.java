package evl.statement.normal;

import evl.statement.normal.NormalStmt;
import common.ElementInfo;

import evl.type.Type;
import evl.type.special.PointerType;
import evl.variable.SsaVariable;

/**
 *
 * @author urs
 */
public class StackMemoryAlloc extends NormalStmt implements SsaGenerator {

  private SsaVariable variable;

  public StackMemoryAlloc(ElementInfo info, SsaVariable variable) {
    super(info);
    assert ( variable.getType().getRef() instanceof PointerType );
    this.variable = variable;
  }

  public Type getType() {
    Type varType = variable.getType().getRef();
    assert ( varType instanceof PointerType );
    return ( (PointerType) varType ).getType().getRef();
  }

  @Override
  public SsaVariable getVariable() {
    return variable;
  }

  public void setVariable(SsaVariable variable) {
    this.variable = variable;
  }
}
