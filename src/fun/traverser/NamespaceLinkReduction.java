package fun.traverser;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.DummyLinkTarget;
import fun.expression.reference.RefItem;
import fun.expression.reference.RefName;
import fun.expression.reference.Reference;
import fun.knowledge.KnowChild;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;

/**
 * Follows offset when link is to namespace until it finds a different object.
 * 
 * Example: makes foo.bar() to bar()
 * 
 * @author urs
 * 
 */
public class NamespaceLinkReduction extends DefTraverser<Void, Void> {

  public static void process(Fun inst) {
    NamespaceLinkReduction reduction = new NamespaceLinkReduction();
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    Fun item = obj.getLink();
    assert (!(item instanceof DummyLinkTarget));
    while (item instanceof Namespace) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      RefName name = (RefName) next;
      ((Namespace) item).getChildren().find(name.getName());
      assert (item != null); // type checker should find it?
    }
    if (item instanceof RizzlyFile) {
      RefItem next = obj.getOffset().get(0);
      obj.getOffset().remove(0);
      if (!(next instanceof RefName)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected named offset, got: " + next.getClass().getCanonicalName());
      }
      RefName name = (RefName) next;
      KnowChild kfc = new KnowChild();
      item = kfc.get(item, name.getName());
      assert (item != null); // type checker should find it?
    }
    obj.setLink((Named) item);
    return super.visitReference(obj, param);
  }

}
