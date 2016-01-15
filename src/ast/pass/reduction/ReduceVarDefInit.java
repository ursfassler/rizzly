package ast.pass.reduction;

import java.util.List;

import main.Configuration;
import ast.data.Namespace;
import ast.data.statement.Statement;
import ast.data.statement.VarDefInitStmt;
import ast.data.variable.FuncVariable;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.RError;

public class ReduceVarDefInit extends AstPass {
  public ReduceVarDefInit(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceVarDefInitWorker worker = new ReduceVarDefInitWorker();
    worker.traverse(ast, null);
  }

}

class ReduceVarDefInitWorker extends StmtReplacer<Void> {

  @Override
  protected List<Statement> visitVarDefInitStmt(VarDefInitStmt obj, Void param) {
    // FIXME move initialization of variable to here
    RError.ass(obj.variable.size() == 1, obj.getInfo(), "expected exactly 1 variable, got " + obj.variable.size());
    FuncVariable var = obj.variable.get(0);
    return list(new ast.data.statement.VarDefStmt(obj.getInfo(), var));
  }

}
