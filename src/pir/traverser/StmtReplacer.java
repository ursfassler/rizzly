package pir.traverser;

import java.util.ArrayList;
import java.util.List;

import pir.DefTraverser;
import pir.cfg.BasicBlock;
import pir.cfg.BasicBlockEnd;
import pir.function.Function;
import pir.other.Program;
import pir.statement.Statement;

public class StmtReplacer<P> extends DefTraverser<List<Statement>, P> {

  protected List<Statement> add(Statement stmt) {
    ArrayList<Statement> ret = new ArrayList<Statement>();
    ret.add(stmt);
    return ret;
  }

  protected List<Statement> add(Statement stmt1, Statement stmt2) {
    ArrayList<Statement> ret = new ArrayList<Statement>();
    ret.add(stmt1);
    ret.add(stmt2);
    return ret;
  }

  @Override
  protected List<Statement> visitProgram(Program obj, P param) {
    for (Function func : obj.getFunction()) {
      visit(func, param);
    }
    return null;
  }

  @Override
  protected List<Statement> visitBasicBlock(BasicBlock obj, P param) {
    ArrayList<Statement> stmts = new ArrayList<Statement>(obj.getCode());
    obj.getCode().clear();

    for (Statement stmt : stmts) {
      List<Statement> list = visit(stmt, param);
      obj.getCode().addAll(list);
    }

    List<Statement> list = visit(obj.getEnd(), param);
    obj.getCode().addAll(list);

    return null;
  }

  @Override
  protected List<Statement> visitBasicBlockEnd(BasicBlockEnd obj, P param) {
    List<Statement> ret = super.visitBasicBlockEnd(obj, param);
    if (ret == null) {
      ret = new ArrayList<Statement>();
    }
    return ret;
  }

  @Override
  protected List<Statement> visitStatement(Statement obj, P param) {
    List<Statement> ret = super.visitStatement(obj, param);
    if (ret == null) {
      ret = add(obj);
    }
    return ret;
  }

}
