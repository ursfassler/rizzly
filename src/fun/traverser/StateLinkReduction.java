package fun.traverser;

import java.util.HashSet;
import java.util.Set;

import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.ReferenceLinked;
import fun.function.impl.FuncPrivateRet;
import fun.function.impl.FuncPrivateVoid;
import fun.hfsm.State;
import fun.knowledge.KnowFunChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.variable.StateVariable;

/**
 * Changes references to deepest state, e.g. _top.A.B -> B
 * 
 * @author urs
 * 
 */
public class StateLinkReduction extends DefTraverser<Void, Void> {
  private final KnowFunChild kc;
  static final private Set<Class<? extends Named>> stop = new HashSet<Class<? extends Named>>();

  {
    stop.add(StateVariable.class);
    // stop.add(HfsmQueryFunction.class);
    stop.add(FuncPrivateRet.class);
    stop.add(FuncPrivateVoid.class);
  }

  public StateLinkReduction(KnowledgeBase kb) {
    kc = kb.getEntry(KnowFunChild.class);
  }

  public static void process(Fun inst, KnowledgeBase kb) {
    StateLinkReduction reduction = new StateLinkReduction(kb);
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Void param) {
    Named item = obj.getLink();
    if (item instanceof State) {
      while (!obj.getOffset().isEmpty()) {
        RefItem next = obj.getOffset().pop();
        RefName name = (RefName) next;

        item = (Named) kc.get(item, name.getName());
        assert (item != null);
        // TODO why don't we do it as long as item is instance of State?
        if (stop.contains(item.getClass())) {
          break;
        }
      }
      obj.setLink(item);
      obj.getOffset().clear();
    }
    return super.visitReferenceLinked(obj, param);
  }
}
