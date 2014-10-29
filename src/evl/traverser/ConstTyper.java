package evl.traverser;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowType;
import evl.knowledge.KnowledgeBase;
import evl.type.Type;
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
  private final KnowBaseItem kbi;

  public ConstTyper(KnowledgeBase kb) {
    super();
    this.kt = kb.getEntry(KnowType.class);
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  public static void process(Evl evl, KnowledgeBase kb) {
    ConstTyper replace = new ConstTyper(kb);
    replace.traverse(evl, null);
  }

  private static boolean isOpenType(Type type) {
    return ((type instanceof AnyType) || (type instanceof NaturalType) || (type instanceof IntegerType));
  }

  @Override
  protected Void visitConstant(Constant obj, Void param) {
    if (isOpenType(obj.getType().getLink())) {
      Type ct = kt.get(obj.getDef());

      ct = kbi.getType(ct);

      obj.setType(new SimpleRef<Type>(obj.getInfo(), ct));
    }
    super.visitConstant(obj, param);
    return null;
  }

}
