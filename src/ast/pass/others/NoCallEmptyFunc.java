package ast.pass.others;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.function.FunctionProperty;
import ast.data.statement.CallStmt;
import ast.data.statement.Statement;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.other.StmtReplacer;
import error.ErrorType;
import error.RError;

public class NoCallEmptyFunc extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    NoCallEmptyFuncWorker worker = new NoCallEmptyFuncWorker();
    worker.traverse(ast, null);
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
        return new AstList<Statement>();
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
    return func.body.statements.isEmpty() && (func.property == FunctionProperty.Private);

  }
}
