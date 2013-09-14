package evl.knowledge;

import java.util.HashMap;
import java.util.Map;

import evl.DefTraverser;
import evl.expression.Expression;
import evl.statement.normal.VarDefInitStmt;
import evl.variable.SsaVariable;

public class KnowWriter extends KnowledgeEntry {
  private KnowledgeBase kb;
  final private Map<SsaVariable, Expression> writer = new HashMap<SsaVariable, Expression>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Expression get(SsaVariable var) {
    if (!writer.containsKey(var)) {
      update();
    }
    Expression expr = writer.get(var);
    assert (expr != null);
    return expr;
  }

  private void update() {
    WriterCollector collector = new WriterCollector();
    writer.clear();
    collector.traverse(kb.getRoot(), writer);
  }

}

class WriterCollector extends DefTraverser<Void, Map<SsaVariable, Expression>> {

  @Override
  protected Void visitVarDefInitStmt(VarDefInitStmt obj, Map<SsaVariable, Expression> param) {
    param.put(obj.getVariable(), obj.getInit());
    return null;
  }

}
