package evl.traverser.iocheck;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.Reference;
import evl.statement.Assignment;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Returns for every function if it reads state. It does not check called functions nor it uses information from the
 * function type or header.
 * 
 * @author urs
 * 
 */
public class StateReaderInfo extends DefTraverser<Void, Void> {
  private boolean readState = false;

  public static boolean get(Evl inst) {
    StateReaderInfo reduction = new StateReaderInfo();
    reduction.traverse(inst, null);
    return reduction.readState;
  }

  private boolean isStateVariable(Variable var) {
    return var instanceof StateVariable;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void param) {
    visitList(obj.getLeft().getOffset(), param);
    visit(obj.getRight(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() instanceof Variable) {
      Variable var = (Variable) obj.getLink();
      if (isStateVariable(var)) {
        readState = true;
      }
    }
    return super.visitReference(obj, param);
  }

}
