package evl.traverser;

import evl.DefTraverser;
import evl.Evl;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
import evl.type.TypeRef;
import evl.type.special.AnyType;
import evl.type.special.IntegerType;
import evl.type.special.NaturalType;
import evl.variable.Constant;

/**
 * Sets and constrains types for all constants
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
    ConstTyper replace = new ConstTyper(kb);
    replace.traverse(evl, null);
  }

  private static boolean isOpenType(Type type) {
    return ((type instanceof AnyType) || (type instanceof NaturalType) || (type instanceof IntegerType));
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    if (isOpenType(obj.getType().getRef())) {
      Type ct = kt.get(obj.getDef());
      obj.setType(new TypeRef(obj.getInfo(), ct));
    }
    super.visitConstant(obj, param);
    return null;
  }

}