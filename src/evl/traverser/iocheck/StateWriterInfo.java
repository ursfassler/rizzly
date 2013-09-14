package evl.traverser.iocheck;

import evl.DefTraverser;
import evl.Evl;
import evl.statement.normal.Assignment;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Returns for every function if it writes state. It does not check called functions nor it uses information from the
 * function type or header.
 *
 * @author urs
 *
 */
public class StateWriterInfo extends DefTraverser<Void, Void> {
  private boolean writeState = false;

  public static boolean get(Evl inst) {
    StateWriterInfo reduction = new StateWriterInfo();
    reduction.traverse(inst, null);
    return reduction.writeState;
  }

  private boolean isStateVariable(Variable var) {
    return var instanceof StateVariable;
  }

  @Override
  protected Void visitAssignment(Assignment obj, Void param) {
    if (obj.getLeft().getLink() instanceof Variable) {
      Variable var = (Variable) obj.getLeft().getLink();
      if (isStateVariable(var)) {
        writeState = true;
      }
    }
    return null;
  }

}
