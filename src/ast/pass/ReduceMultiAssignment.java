package ast.pass;

import java.util.List;

import pass.AstPass;
import ast.data.Namespace;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.Statement;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.StmtReplacer;
import error.ErrorType;
import error.RError;

public class ReduceMultiAssignment extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceMultiAssignmentWorker worker = new ReduceMultiAssignmentWorker();
    worker.traverse(ast, null);
  }

}

class ReduceMultiAssignmentWorker extends StmtReplacer<Void> {

  @Override
  protected List<Statement> visitAssignmentMulti(AssignmentMulti obj, Void param) {
    switch (obj.left.size()) {
      case 0: {
        RError.err(ErrorType.Fatal, obj.getInfo(), "assignment needs at least one item on the left side");
        return null;
      }
      case 1: {
        return list(new AssignmentSingle(obj.getInfo(), obj.left.get(0), obj.right));
      }
      default: {
        return list(obj);
      }
    }
  }

}
