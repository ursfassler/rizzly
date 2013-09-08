package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.type.Type;
import evl.type.base.Range;
import java.math.BigInteger;

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

  @Override
  protected Type visitRange(Range obj, Void param) {
    return new Range(obj.getLow(), obj.getHigh());
  }
  
  

}
