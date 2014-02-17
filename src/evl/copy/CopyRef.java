package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.RefCall;
import evl.expression.reference.RefIndex;
import evl.expression.reference.RefItem;
import evl.expression.reference.RefName;

public class CopyRef extends NullTraverser<RefItem, Void> {
  private CopyEvl cast;

  public CopyRef(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected RefItem visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected RefItem visitRefCall(RefCall obj, Void param) {
    return new RefCall(obj.getInfo(), cast.copy(obj.getActualParameter()));
  }

  @Override
  protected RefItem visitRefName(RefName obj, Void param) {
    return new RefName(obj.getInfo(), obj.getName());
  }

  @Override
  protected RefItem visitRefIndex(RefIndex obj, Void param) {
    return new RefIndex(obj.getInfo(), cast.copy(obj.getIndex()));
  }

}
