package ast.pass.reduction;

import java.util.List;

import ast.data.Namespace;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.MultiAssignment;
import ast.data.statement.Statement;
import ast.dispatcher.other.StmtReplacer;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import error.ErrorType;
import error.RError;

public class ReduceMultiAssignment implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    ReduceMultiAssignmentWorker worker = new ReduceMultiAssignmentWorker();
    worker.traverse(ast, null);
  }

}

class ReduceMultiAssignmentWorker extends StmtReplacer<Void> {

  @Override
  protected List<Statement> visitAssignmentMulti(MultiAssignment obj, Void param) {
    switch (obj.getLeft().size()) {
      case 0: {
        RError.err(ErrorType.Fatal, "assignment needs at least one item on the left side", obj.metadata());
        return null;
      }
      case 1: {
        AssignmentSingle ass = new AssignmentSingle(obj.getLeft().get(0), obj.getRight());
        ass.metadata().add(obj.metadata());
        return list(ass);
      }
      default: {
        return list(obj);
      }
    }
  }

}
