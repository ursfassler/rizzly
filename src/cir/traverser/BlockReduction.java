package cir.traverser;

import java.util.ArrayList;
import java.util.List;

import cir.CirBase;
import cir.DefTraverser;
import cir.statement.Block;
import cir.statement.Statement;

public class BlockReduction extends DefTraverser<Void, Void> {

  public static void process(CirBase obj) {
    BlockReduction reduction = new BlockReduction();
    reduction.traverse(obj, null);
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    List<Statement> old = new ArrayList<Statement>(obj.getStatement());
    obj.getStatement().clear();

    for (Statement stmt : old) {
      visitStatement(stmt, null);
      if (stmt instanceof Block) {
        obj.getStatement().addAll(((Block) stmt).getStatement());
      } else {
        obj.getStatement().add(stmt);
      }
    }

    return null;
  }

}
