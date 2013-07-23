package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.type.Type;

public class CopyType extends NullTraverser<Type, Void> {
  private CopyEvl cast;

  public CopyType(CopyEvl cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Type visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

}
