package evl.traverser;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.other.Named;
import evl.other.Namespace;

/**
 * Follows offset when link is to namespace until it finds a different object.
 * 
 * Example: makes foo.bar() to bar()
 * 
 * @author urs
 * 
 */
public class LinkReduction extends DefTraverser<Void, Void> {

  public static void process(Evl inst) {
    LinkReduction reduction = new LinkReduction();
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Evl item = obj.getLink();
    while (item instanceof Namespace) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        // TODO check it with typechecker
        RError.err(ErrorType.Fatal, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName() + " (and why did the typechecker not find it?)");
      }
      RefName name = (RefName) next;
      item = ((Namespace) item).getChildren().find(name.getName());
      assert (item != null); // type checker should find it?
    }
    obj.setLink((Named) item);
    return super.visitReference(obj, param);
  }

}
