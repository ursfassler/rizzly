package cir.traverser;

import cir.CirBase;
import cir.NullTraverser;
import cir.type.ArrayType;
import cir.type.EnumType;
import cir.type.IntType;
import cir.type.StructType;
import cir.type.Type;
import cir.type.TypeAlias;
import cir.type.TypeRef;
import cir.type.UnionType;
import cir.type.VoidType;

public class TypeSizeGetter extends NullTraverser<Integer, Void> {

  private static int ByteAlignment = 1; // TODO get this information from somewhere else

  public static int get(Type type) {
    TypeSizeGetter getter = new TypeSizeGetter();
    return getter.traverse(type, null);
  }

  private int roundUp(int tsize) {
    if (tsize % ByteAlignment != 0) {
      int nsize = tsize + (ByteAlignment - (tsize % ByteAlignment));
      assert (nsize > tsize);
      assert (nsize % ByteAlignment == 0);
      return nsize;
    }
    return tsize;
  }

  @Override
  protected Integer visitTypeRef(TypeRef obj, Void param) {
    return visit(obj.getRef(), param);
  }

  @Override
  protected Integer visitDefault(CirBase obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitTypeAlias(TypeAlias obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitVoidType(VoidType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitEnumType(EnumType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitStructType(StructType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitUnionType(UnionType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitArrayType(ArrayType obj, Void param) {
    int tsize = visit(obj.getType(), param);
    tsize = roundUp(tsize);
    return obj.getSize() * tsize;
  }

  @Override
  protected Integer visitIntType(IntType obj, Void param) {
    return obj.getBytes();
  }

}
