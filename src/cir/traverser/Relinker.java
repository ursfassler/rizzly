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

import java.util.Map;

import cir.Cir;
import cir.DefTraverser;
import cir.expression.reference.Referencable;
import cir.expression.reference.Reference;
import cir.type.Type;
import cir.type.TypeRef;

public class Relinker extends DefTraverser<Void, Map<? extends Referencable, ? extends Referencable>> {

  public static void process(Cir obj, Map<? extends Referencable, ? extends Referencable> map) {
    Relinker relinker = new Relinker();
    relinker.traverse(obj, map);
  }

  private Referencable replace(Referencable ref, Map<? extends Referencable, ? extends Referencable> param) {
    Referencable ntarget = param.get(ref);
    if (ntarget != null) {
      return ntarget;
    } else {
      return ref;
    }
  }

  @Override
  protected Void visitTypeRef(TypeRef obj, Map<? extends Referencable, ? extends Referencable> param) {
    super.visitTypeRef(obj, param);
    obj.setRef((Type) replace(obj.getRef(), param));
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Map<? extends Referencable, ? extends Referencable> param) {
    super.visitReference(obj, param);
    obj.setRef(replace(obj.getRef(), param));
    return null;
  }

}
