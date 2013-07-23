package cir.knowledge;

import java.util.HashSet;
import java.util.Set;

import cir.CirBase;
import cir.NullTraverser;
import cir.other.Variable;
import cir.type.EnumType;
import cir.type.StructType;
import cir.type.UnionType;

public class KnowChild extends KnowledgeEntry {

  @Override
  public void init(KnowledgeBase base) {
  }

  public static CirBase find(CirBase sub, String name) {
    KnowCChildTraverser kct = new KnowCChildTraverser();
    Set<CirBase> rset = kct.traverse(sub, name);
    if (rset.isEmpty()) {
      return null;
    }
    if (rset.size() == 1) {
      return rset.iterator().next();
    }
    return null;
  }

}

class KnowCChildTraverser extends NullTraverser<Set<CirBase>, String> {

  public Set<CirBase> retopt(CirBase res) {
    assert (res != null);
    Set<CirBase> rset = new HashSet<CirBase>();
    rset.add(res);
    return rset;
  }

  @Override
  protected Set<CirBase> visitDefault(CirBase obj, String param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Set<CirBase> visitEnumType(EnumType obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<CirBase> visitVariable(Variable obj, String param) {
    return visit(obj.getType(), param);
  }

  @Override
  protected Set<CirBase> visitStructType(StructType obj, String param) {
    return retopt(obj.find(param));
  }

  @Override
  protected Set<CirBase> visitUnionType(UnionType obj, String param) {
    return retopt(obj.find(param));
  }

}
