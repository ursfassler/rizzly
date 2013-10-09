package evl.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evl.DefTraverser;
import evl.expression.reference.Reference;
import evl.statement.Statement;
import evl.variable.SsaVariable;

public class KnowSsaUsage extends KnowledgeEntry {

  private KnowledgeBase kb;
  final private Map<SsaVariable, List<Statement>> use = new HashMap<SsaVariable, List<Statement>>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public List<Statement> get(SsaVariable var) {
    if (!use.containsKey(var)) {
      update();
    }
    assert (use.containsKey(var));
    return use.get(var);
  }

  private void update() {
    use.clear();
    UseGetter getter = new UseGetter(use);
    getter.traverse(kb.getRoot(), null);
  }
}

class UseGetter extends DefTraverser<Void, Statement> {

  final private Map<SsaVariable, List<Statement>> use;

  public UseGetter(Map<SsaVariable, List<Statement>> use) {
    super();
    this.use = use;
  }

  @Override
  protected Void visitStatement(Statement obj, Statement param) {
    assert (param == null);
    return super.visitStatement(obj, obj);
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, Statement param) {
    List<Statement> stmts = use.get(obj);
    if (stmts == null) {
      stmts = new ArrayList<Statement>();
      use.put(obj, stmts);
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Statement param) {
    if ((obj.getLink() instanceof SsaVariable) && (param != null)) { // could be from a transition
      assert (param != null);
      SsaVariable var = (SsaVariable) obj.getLink();
      List<Statement> stmts = use.get(var);
      if (stmts == null) {
        stmts = new ArrayList<Statement>();
        use.put(var, stmts);
      }
      if (!stmts.contains(param)) {
        stmts.add(param);
      }
    }
    super.visitReference(obj, param);
    return null;
  }
}
