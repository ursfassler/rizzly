package fun.traverser;

import error.ErrorType;
import error.RError;
import fun.DefGTraverser;
import fun.Fun;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.ReferenceLinked;
import fun.expression.reference.ReferenceUnlinked;
import fun.other.Named;
import fun.other.Namespace;

/**
 * Follows offset when link is to namespace until it finds a different object.
 * 
 * Example: makes foo.bar() to bar()
 * 
 * @author urs
 * 
 */
public class NamespaceLinkReduction extends DefGTraverser<Void, Void> {

  public static void process(Fun inst) {
    NamespaceLinkReduction reduction = new NamespaceLinkReduction();
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReferenceLinked(ReferenceLinked obj, Void param) {
    Named item = obj.getLink();
    while (item instanceof Namespace) {
      RefItem next = obj.getOffset().pop();
      if (!(next instanceof RefName)) {
        // TODO check it with typechecker
        RError.err(ErrorType.Fatal, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName() + " (and why did the typechecker not find it?)");
      }
      RefName name = (RefName) next;
      item = ((Namespace) item).find(name.getName());
      assert (item != null); // type checker should find it?
    }
    obj.setLink(item);
    return super.visitReferenceLinked(obj, param);
  }

  @Override
  protected Void visitReferenceUnlinked(ReferenceUnlinked obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

}
