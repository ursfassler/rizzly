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

package ast.pass.debug;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.composition.ComponentUse;
import ast.data.component.composition.ImplComposition;
import ast.data.component.elementary.ImplElementary;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.reference.LinkedReferenceWithOffset_Implementation;
import ast.data.type.Type;
import ast.dispatcher.NullDispatcher;

public class CompCascadeDepth extends NullDispatcher<Integer, Void> {

  @Override
  protected Integer visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Integer visitNamespace(Namespace obj, Void param) {
    int max = 0;
    for (Ast itr : obj.children) {
      max = Math.max(max, visit(itr, param));
    }
    return max;
  }

  @Override
  protected Integer visitType(Type obj, Void param) {
    return 0;
  }

  @Override
  protected Integer visitCompUse(ComponentUse obj, Void param) {
    return 0;
  }

  @Override
  protected Integer visitImplElementary(ImplElementary obj, Void param) {
    int max = 0;
    for (ComponentUse itr : obj.component) {
      max = Math.max(max, visit(itr.compRef.getTarget(), param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplComposition(ImplComposition obj, Void param) {
    int max = 0;
    for (ComponentUse itr : obj.component) {
      max = Math.max(max, visit(itr.compRef.getTarget(), param));
    }
    return max + 1;
  }

  @Override
  protected Integer visitImplHfsm(ImplHfsm obj, Void param) {
    return super.visitImplHfsm(obj, param);
  }

  @Override
  protected Integer visitReference(LinkedReferenceWithOffset_Implementation obj, Void param) {
    assert (obj.getOffset().isEmpty());
    return visit(obj.getLink(), param);
  }

}
