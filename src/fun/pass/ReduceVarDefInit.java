package fun.pass;

import java.util.List;

import pass.EvlPass;
import error.RError;
import evl.data.Namespace;
import evl.data.statement.Statement;
import evl.data.statement.VarDefInitStmt;
import evl.data.variable.FuncVariable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.StmtReplacer;

public class ReduceVarDefInit extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceVarDefInitWorker worker = new ReduceVarDefInitWorker();
    worker.traverse(evl, null);
  }

}

class ReduceVarDefInitWorker extends StmtReplacer<Void> {

  @Override
  protected List<Statement> visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    // FIXME move initialization of variable to here
    RError.ass(obj.variable.size() == 1, obj.getInfo(), "expected exactly 1 variable, got " + obj.variable.size());
    FuncVariable var = obj.variable.get(0);
    return list(new evl.data.statement.VarDefStmt(obj.getInfo(), var));
  }

}
