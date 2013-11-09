package fun.traverser;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.knowledge.KnowFunChild;
import fun.knowledge.KnowledgeBase;
import fun.other.Named;
import fun.type.base.EnumElement;
import fun.type.base.EnumType;

/**
 * Changes references to enums, e.g. Weekday.Tuesday -> Tuesday
 * 
 * @author urs
 * 
 */
public class EnumLinkReduction extends DefTraverser<Void, Void> {
  private final KnowFunChild kc;

  public EnumLinkReduction(KnowledgeBase kb) {
    kc = kb.getEntry(KnowFunChild.class);
  }

  public static void process(Fun inst, KnowledgeBase kb) {
    EnumLinkReduction reduction = new EnumLinkReduction(kb);
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Named item = obj.getLink();
    if (item instanceof EnumType) {
      if (!obj.getOffset().isEmpty()) {
        RefItem next = obj.getOffset().pop();
        if (!(next instanceof RefName)) {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
        }
        Fun elem = kc.get(item, ((RefName) next).getName());
        if (elem instanceof EnumElement) {
          obj.setLink((EnumElement) elem);
        } else {
          RError.err(ErrorType.Error, obj.getInfo(), "Expected enumerator element, got: " + elem.getClass().getCanonicalName());
        }
      }
    }
    return super.visitReference(obj, param);
  }
}
