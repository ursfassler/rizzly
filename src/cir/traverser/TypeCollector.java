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

import java.util.Collection;

import cir.DefTraverser;
import cir.type.NamedElement;
import cir.type.Type;
import cir.type.TypeRef;

public class TypeCollector extends DefTraverser<Void, Collection<Type>> {

  @Override
  protected Void visitType(Type obj, Collection<Type> param) {
    if (param.contains(obj)) {
      return null;
    } else {
      param.add(obj);
      return super.visitType(obj, param);
    }
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Collection<Type> param) {
    visit(obj.getRef(), param);
    return null;
  }

  @Override
  protected Void visitNamedElement(NamedElement obj, Collection<Type> param) {
    visit(obj.getType(), param);
    return null;
  }

}
