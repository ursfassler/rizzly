package evl.traverser.debug;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.Reference;
import evl.other.CompUse;
import evl.other.Component;
import evl.other.ImplElementary;

public class CompCascadeDepth extends NullTraverser<Integer, Void> {

  public static int get(Component root) {
    CompCascadeDepth counter = new CompCascadeDepth();
    return counter.traverse(root, null);
  }

  @Override
  protected Integer visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitImplElementary(ImplElementary obj, Void param) {
    int max = 0;
    for (CompUse itr : obj.getComponent()) {
      max = Math.max(max, visit(itr.getLink(), param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitReference(Reference obj, Void param) {
    assert( obj.getOffset().isEmpty() );
    return visit( obj.getLink(), param );
  }

}
