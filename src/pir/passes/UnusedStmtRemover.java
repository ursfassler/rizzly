package pir.passes;

import java.util.Set;

import pir.DefTraverser;
import pir.PirObject;
import pir.other.Program;
import pir.statement.Statement;
import util.SimpleGraph;

/**
 * Removes unused statements. This are statements with no dependencies and no input or output
 * 
 * @author urs
 * 
 */
public class UnusedStmtRemover extends DefTraverser<Void, Set<Statement>> {

  public static void process(Program obj, SimpleGraph<PirObject> dependencyGraph) {
    UnusedStmtRemover changer = new UnusedStmtRemover();
    changer.traverse(obj, null);
  }

}
