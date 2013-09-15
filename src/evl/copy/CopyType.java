package evl.copy;

import evl.Evl;
import evl.NullTraverser;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumType;
import evl.type.base.Range;
import evl.type.base.StringType;
import evl.type.composed.RecordType;
import evl.type.special.PointerType;

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

  @Override
  protected Type visitRecordType(RecordType obj, Void param) {
    return new RecordType(obj.getInfo(), obj.getName(), cast.copy(obj.getElement().getList()));
  }

  @Override
  protected Type visitStringType(StringType obj, Void param) {
    return new StringType();
  }

  @Override
  protected Type visitArrayType(ArrayType obj, Void param) {
    return new ArrayType(obj.getSize(), cast.copy(obj.getType()));
  }

  @Override
  protected Type visitPointerType(PointerType obj, Void param) {
    return new PointerType(cast.copy(obj.getType()));
  }

  @Override
  protected Type visitEnumType(EnumType obj, Void param) {
    return new EnumType( obj.getInfo(), obj.getName(), cast.copy(obj.getElement()));
  }
  
  
}
