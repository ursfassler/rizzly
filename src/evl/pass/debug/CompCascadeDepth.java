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

package evl.pass.debug;

import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.component.composition.ImplComposition;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.expression.reference.Reference;
import evl.data.type.Type;
import evl.traverser.NullTraverser;

public class CompCascadeDepth extends NullTraverser<Integer, Void> {

  @Override
  protected Integer visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitNamespace(Namespace obj, Void param) {
    int max = 0;
    for (Evl itr : obj.children) {
      max = Math.max(max, visit(itr, param));
    }
    return max;
  }

  @Override
  protected Integer visitType(Type obj, Void param) {
    return 0;
  }

  @Override
  protected Integer visitCompUse(CompUse obj, Void param) {
    return 0;
  }

  @Override
  protected Integer visitImplElementary(ImplElementary obj, Void param) {
    int max = 0;
    for (CompUse itr : obj.component) {
      max = Math.max(max, visit(itr.instance.link, param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplComposition(ImplComposition obj, Void param) {
    int max = 0;
    for (CompUse itr : obj.component) {
      max = Math.max(max, visit(itr.instance.link, param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplHfsm(ImplHfsm obj, Void param) {
    return super.visitImplHfsm(obj, param);
  }

  @Override
  protected Integer visitReference(Reference obj, Void param) {
    assert (obj.offset.isEmpty());
    return visit(obj.link, param);
  }

}
