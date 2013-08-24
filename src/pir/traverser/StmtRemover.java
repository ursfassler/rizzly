package pir.traverser;

import java.util.HashSet;
import java.util.Set;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.other.Program;
import pir.statement.Statement;

/**
 * Removes unused statements. This are statements with no dependencies and no input or output
 * 
 * @author urs
 * 
 */
public class StmtRemover extends DefTraverser<Void, Set<Statement>> {

  public static Set<Statement> process(Program obj, Set<Statement> removable) {
    removable = new HashSet<Statement>(removable);
    StmtRemover changer = new StmtRemover();
    changer.traverse(obj, removable);
    return removable;
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, Set<Statement> param) {
    Set<Statement> remove = new HashSet<Statement>(param);

    param.removeAll(obj.getPhi());
    obj.getPhi().removeAll(remove);

    param.removeAll(obj.getCode());
    obj.getCode().removeAll(remove);

    assert (!remove.contains(obj.getEnd()));

    return null;
  }

}
