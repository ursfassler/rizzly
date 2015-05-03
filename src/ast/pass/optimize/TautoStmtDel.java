package ast.pass.optimize;

import java.util.List;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.expression.BoolValue;
import ast.data.statement.IfOption;
import ast.data.statement.IfStmt;
import ast.data.statement.Statement;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.specification.ConstantExpression;
import ast.traverser.other.StmtReplacer;

public class TautoStmtDel extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TautoStmtDelWorker worker = new TautoStmtDelWorker();
    worker.traverse(ast, null);
  }

}

class TautoStmtDelWorker extends StmtReplacer<Void> {
  @Override
  protected List<Statement> visitIfStmt(IfStmt obj, Void param) {
    super.visitIfStmt(obj, param);

    AstList<IfOption> keep = new AstList<IfOption>();

    for (IfOption opt : obj.option) {
      if (ConstantExpression.INSTANCE.isSatisfiedBy(opt.condition)) {
        BoolValue value = (BoolValue) opt.condition;
        if (value.value) {
          obj.defblock = opt.code;
          break;
        }
      } else {
        keep.add(opt);
      }
    }

    if (keep.isEmpty()) {
      List<Statement> ret = new AstList<Statement>();
      ret.add(obj.defblock);
      return ret;
    } else {
      obj.option.clear();
      obj.option.addAll(keep);
      return null;
    }
  }

}
