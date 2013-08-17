package pir.traverser;

import java.util.List;

import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Statement;
import pir.statement.StoreStmt;

/**
 * Replaces write accesses to global variables with store operation
 * 
 * @author urs
 * 
 */
public class GlobalWriteExtracter extends StmtReplacer<Void> {

  public static void process(Program obj) {
    GlobalWriteExtracter changer = new GlobalWriteExtracter();
    changer.traverse(obj, null);
  }

  @Override
  protected List<Statement> visitAssignment(Assignment obj, Void param) {
    if (obj.getVariable() instanceof StateVariable) {
      VarRef src = new VarRef(obj.getSrc());
      StoreStmt store = new StoreStmt((StateVariable) obj.getVariable(), src);
      return add(store);
    } else {
      return super.visitAssignment(obj, param);
    }
  }

}
