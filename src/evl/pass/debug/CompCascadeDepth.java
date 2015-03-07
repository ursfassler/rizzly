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

import evl.Evl;
import evl.NullTraverser;
import evl.composition.ImplComposition;
import evl.expression.reference.Reference;
import evl.hfsm.ImplHfsm;
import evl.other.CompUse;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.type.Type;

public class CompCascadeDepth extends NullTraverser<Integer, Void> {

  @Override
  protected Integer visitDefault(Evl obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitNamespace(Namespace obj, Void param) {
    int max = 0;
    for (Evl itr : obj.getChildren()) {
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
    for (CompUse itr : obj.getComponent()) {
      max = Math.max(max, visit(itr.getLink(), param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplComposition(ImplComposition obj, Void param) {
    int max = 0;
    for (CompUse itr : obj.getComponent()) {
      max = Math.max(max, visit(itr.getLink(), param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplHfsm(ImplHfsm obj, Void param) {
    return super.visitImplHfsm(obj, param);
  }

  @Override
  protected Integer visitReference(Reference obj, Void param) {
    assert (obj.getOffset().isEmpty());
    return visit(obj.getLink(), param);
  }

}