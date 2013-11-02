package cir.traverser;

import java.util.LinkedList;

import cir.CirBase;
import cir.DefTraverser;
import cir.statement.Block;
import cir.statement.Statement;
import cir.statement.VarDefStmt;

public class VarDeclToTop extends DefTraverser<Void, Void> {

  public static void process(CirBase cprog) {
    VarDeclToTop cVarDeclToTop = new VarDeclToTop();
    cVarDeclToTop.traverse(cprog, null);
  }

  @Override
  protected Void visitBlock(Block obj, Void param) {
    LinkedList<Statement> nlist = new LinkedList<Statement>();
    for (Statement itr : obj.getStatement()) {
      visit(itr, param);
      if (itr instanceof VarDefStmt) {
        nlist.addFirst(itr);
      } else {
        nlist.addLast(itr);
      }
    }
    obj.getStatement().clear();
    obj.getStatement().addAll(nlist);
    return null;
  }

}
