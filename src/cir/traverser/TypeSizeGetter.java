/**
 *  This file is part of Rizzly.
 *
 *  Rizzly is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Rizzly is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Rizzly.  If not, see <http://www.gnu.org/licenses/>.
 */

package cir.traverser;

import cir.CirBase;
import cir.NullTraverser;
import cir.type.ArrayType;
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
