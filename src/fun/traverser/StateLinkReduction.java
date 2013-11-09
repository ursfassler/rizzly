package fun.traverser;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.hfsm.State;
import fun.knowledge.KnowFunChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;

/**
 * Changes references to deepest state, e.g. _top.A.B -> B
 * 
 * @author urs
 * 
 */
public class StateLinkReduction extends DefTraverser<Void, Void> {
  private final KnowFunChild kc;

  public StateLinkReduction(KnowledgeBase kb) {
    kc = kb.getEntry(KnowFunChild.class);
  }

  public static void process(Fun inst, KnowledgeBase kb) {
    StateLinkReduction reduction = new StateLinkReduction(kb);
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Named item = obj.getLink();
    if (item instanceof State) {
      while (!obj.getOffset().isEmpty()) {
        RefItem next = obj.getOffset().pop();
        RefName name = (RefName) next;

        item = (Named) kc.get(item, name.getName());
        assert (item != null);
        if (!(item instanceof State)) {
          break;
        }
      }
      obj.setLink(item);
      obj.getOffset().clear();
    }
    return super.visitReference(obj, param);
  }
}
