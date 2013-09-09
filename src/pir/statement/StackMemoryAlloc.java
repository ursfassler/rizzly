package pir.statement;

import pir.other.SsaVariable;
import pir.type.PointerType;
import pir.type.Type;

/**
 *
 * @author urs
 */
public class StackMemoryAlloc extends VariableGeneratorStmt {

  public StackMemoryAlloc(SsaVariable addr) {
    super(addr);
    assert ( addr.getType().getRef() instanceof PointerType );
  }

  public Type getType() {
    Type varType = getVariable().getType().getRef();
    assert ( varType instanceof PointerType );
    return ( (PointerType) varType ).getType().getRef();
  }
}
