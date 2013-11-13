package evl.traverser;

import evl.DefTraverser;
import evl.Evl;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.AnyType;
import evl.variable.Constant;

/**
 * Sets types for all constants
 * 
 */
public class ConstTyper extends DefTraverser<Void, Void> {
  private final KnowType kt;

  public ConstTyper(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
    KnowBaseItem kbi = kb.getEntry(KnowBaseItem.class);

    if (kbi.findItem(AnyType.NAME) == null) {
      // no open type used
      return;
    }

    ConstTyper replace = new ConstTyper(kb);
    replace.traverse(evl, null);
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    if (obj.getType().getRef() instanceof AnyType) {
      Type ct = kt.get(obj.getDef());
      obj.setType(new TypeRef(obj.getInfo(), ct));
    }
    super.visitConstant(obj, param);
    return null;
  }

}
