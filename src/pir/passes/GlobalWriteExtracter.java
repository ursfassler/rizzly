package pir.passes;

import java.util.List;

import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.StateVariable;
import pir.statement.Assignment;
import pir.statement.Statement;
import pir.statement.StoreStmt;
import pir.traverser.StmtReplacer;

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
      assert (obj.getSrc().getOffset().isEmpty());
      StoreStmt store = new StoreStmt(new VarRef((StateVariable) obj.getVariable()), obj.getSrc());
      return add(store);
    } else {
      return super.visitAssignment(obj, param);
    }
  }

}
