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

package ast.dispatcher.other;

import ast.data.Ast;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.data.type.out.IntType;
import ast.data.type.special.VoidType;
import ast.dispatcher.NullDispatcher;

public class TypeSizeGetter extends NullDispatcher<Integer, Void> {

  private static int ByteAlignment = 1; // TODO get this information from

  // somewhere else

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
  protected Integer visitOffsetReference(OffsetReference obj, Void param) {
    return visit(obj.getAnchor(), param);
  }

  @Override
  protected Integer visitLinkedAnchor(LinkedAnchor obj, Void param) {
    return visit(obj.getLink(), param);
  }

  @Override
  protected Integer visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitVoidType(VoidType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitRecordType(RecordType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitUnionType(UnionType obj, Void param) {
    throw new RuntimeException("not yet implemented");
  }

  @Override
  protected Integer visitArrayType(ArrayType obj, Void param) {
    int tsize = visit(obj.type, param);
    tsize = roundUp(tsize);
    return obj.size.intValue() * tsize;
  }

  @Override
  protected Integer visitIntType(IntType obj, Void param) {
    return obj.bytes;
  }

}
