package evl.pass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pass.EvlPass;

import common.Property;

import error.ErrorType;
import error.RError;
import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.statement.CallStmt;
import evl.statement.Statement;
import evl.traverser.StmtReplacer;

public class NoCallEmptyFunc extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    NoCallEmptyFuncWorker worker = new NoCallEmptyFuncWorker();
    worker.traverse(evl, null);
  }

}

class NoCallEmptyFuncWorker extends StmtReplacer<Function> {
  private final Set<Function> keep = new HashSet<Function>();
  private final Set<Function> remove = new HashSet<Function>();

  @Override
  protected List<Statement> visitCallStmt(CallStmt obj, Function param) {
    if (obj.call.link instanceof Function) {
      Function func = (Function) obj.call.link;

      if (!keep.contains(func) && !remove.contains(func)) {
        check(func, param);
      }

      if (keep.contains(func)) {
        return null;
      }
      if (remove.contains(func)) {
        return new EvlList<Statement>();
      }

      RError.err(ErrorType.Fatal, obj.getInfo(), "reached unreachable code");
    }

    return null;
  }

  private void check(Function func, Function param) {
    if (func == param) {
      // recursive function call
      keep.add(func);
      return;
    }
    visit(func, func);
    if (removable(func)) {
      remove.add(func);
    } else {
      keep.add(func);
    }
  }

  private boolean removable(Function func) {
    return func.body.statements.isEmpty() && !func.properties().containsKey(Property.Public) && !func.properties().containsKey(Property.Extern);

  }
}
