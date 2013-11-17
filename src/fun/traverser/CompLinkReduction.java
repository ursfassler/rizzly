package fun.traverser;

import error.ErrorType;
import error.RError;
import fun.Fun;
import fun.NullTraverser;
import fun.composition.ImplComposition;
import fun.expression.reference.RefName;
import fun.expression.reference.RefTemplCall;
import fun.other.Component;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.RizzlyFile;
import fun.variable.CompUse;

/**
 * Changes references to components, e.g. comp.foo.Bar -> Bar
 * 
 * @author urs
 * 
 */
public class CompLinkReduction extends NullTraverser<Void, Void> {

  public static void process(Fun inst) {
    CompLinkReduction reduction = new CompLinkReduction();
    reduction.traverse(inst, null);
  }

  @Override
  protected Void visitDefault(Fun obj, Void param) {
    return null;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Void param) {
    visitItr(obj, param);
    return null;
  }

  @Override
  protected Void visitImplComposition(ImplComposition obj, Void param) {
    visitItr(obj.getComponent(), param);
    return null;
  }

  @Override
  protected Void visitCompUse(CompUse obj, Void param) {
    Named item = obj.getType().getLink();

    while (!obj.getType().getOffset().isEmpty()) {
      if (obj.getType().getOffset().peek() instanceof RefTemplCall) {
        break;
      }
      RefName rn = (RefName) obj.getType().getOffset().pop();
      if (item instanceof RizzlyFile) {
        item = ((RizzlyFile) item).getComp().find(rn.getName());
      } else if (item instanceof Namespace) {
        item = ((Namespace) item).find(rn.getName());
      } else {
        RError.err(ErrorType.Fatal, item.getInfo(), "Unhandled type: " + item.getClass().getCanonicalName());
      }
      assert (item != null);
    }

    assert (item instanceof Component);
    obj.getType().setLink(item);

    return null;
  }

}
