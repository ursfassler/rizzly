package evl.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import evl.DefTraverser;
import evl.expression.reference.Reference;
import evl.statement.Statement;
import evl.variable.SsaVariable;

public class KnowSsaUsage extends KnowledgeEntry {

  private KnowledgeBase kb;
  final private Map<SsaVariable, Set<Statement>> use = new HashMap<SsaVariable, Set<Statement>>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Set<Statement> get(SsaVariable var) {
    if( !use.containsKey(var) ) {
      update();
    }
    assert ( use.containsKey(var) );
    return use.get(var);
  }

  private void update() {
    use.clear();
    UseGetter getter = new UseGetter(use);
    getter.traverse(kb.getRoot(), null);
  }
}

class UseGetter extends DefTraverser<Void, Statement> {

  final private Map<SsaVariable, Set<Statement>> use;

  public UseGetter(Map<SsaVariable, Set<Statement>> use) {
    super();
    this.use = use;
  }

  @Override
  protected Void visitStatement(Statement obj, Statement param) {
    assert ( param == null );
    return super.visitStatement(obj, obj);
  }

  @Override
  protected Void visitSsaVariable(SsaVariable obj, Statement param) {
    Set<Statement> stmts = use.get(obj);
    if( stmts == null ) {
      stmts = new HashSet<Statement>();
      use.put(obj, stmts);
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Statement param) {
    if( obj.getLink() instanceof SsaVariable ) {
      assert ( param != null );
      SsaVariable var = (SsaVariable) obj.getLink();
      Set<Statement> stmts = use.get(var);
      if( stmts == null ) {
        stmts = new HashSet<Statement>();
        use.put(var, stmts);
      }
      stmts.add(param);
    }
    super.visitReference(obj, param);
    return null;
  }
}
