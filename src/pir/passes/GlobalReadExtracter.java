package pir.passes;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.expression.reference.VarRef;
import pir.other.Program;
import pir.other.SsaVariable;
import pir.other.StateVariable;
import pir.statement.LoadStmt;
import pir.statement.Statement;
import pir.type.Type;

import common.NameFactory;

/**
 * Replaces read accesses to global variables with load operation
 * 
 * @author urs
 * 
 */
public class GlobalReadExtracter extends DefTraverser<Void, List<Statement>> {

  public static void process(Program obj) {
    GlobalReadExtracter changer = new GlobalReadExtracter();
    changer.traverse(obj, null);
  }

  @Override
  protected Void visitBasicBlock(BasicBlock obj, List<Statement> param) {
    assert (param == null);
    // we hope that phi statements do not refer state variables

    for (Statement stmt : new ArrayList<Statement>(obj.getCode())) {
      List<Statement> list = new ArrayList<Statement>();
      visit(stmt, list);
      int index = obj.getCode().indexOf(stmt);
      assert (index >= 0);
      obj.getCode().addAll(index, list);
    }

    List<Statement> list = new ArrayList<Statement>();
    visit(obj.getEnd(), list);
    obj.getCode().addAll(list);

    return null;
  }

  @Override
  protected Void visitVarRef(VarRef obj, List<Statement> param) {
    if (obj.getRef() instanceof StateVariable) {
      Type type = obj.getRef().getType();
      SsaVariable var = new SsaVariable(NameFactory.getNew(), type);
      LoadStmt load = new LoadStmt(var, new VarRef((StateVariable) obj.getRef()));
      obj.setRef(var);
      param.add(load);
    }
    return null;
  }

}
