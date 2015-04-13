package fun.pass;

import java.util.List;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Namespace;
import evl.data.statement.AssignmentMulti;
import evl.data.statement.AssignmentSingle;
import evl.data.statement.Statement;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.StmtReplacer;

public class ReduceMultiAssignment extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceMultiAssignmentWorker worker = new ReduceMultiAssignmentWorker();
    worker.traverse(evl, null);
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
