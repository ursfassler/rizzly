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

package ast.pass.reduction.hfsm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ast.data.Ast;
import ast.data.AstList;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.Transition;
import ast.data.function.header.Slot;
import ast.dispatcher.NullDispatcher;
import ast.repository.query.Referencees.TargetResolver;

public class TransitionDict extends NullDispatcher<Void, Void> {
  private Map<State, Map<Slot, AstList<Transition>>> transition = new HashMap<State, Map<Slot, AstList<Transition>>>();

  public AstList<Transition> get(State src, Slot func) {
    Map<Slot, AstList<Transition>> smap = transition.get(src);
    if (smap == null) {
      smap = new HashMap<Slot, AstList<Transition>>();
      transition.put(src, smap);
    }
    AstList<Transition> fmap = smap.get(func);
    if (fmap == null) {
      fmap = new AstList<Transition>();
      smap.put(func, fmap);
    }
    return fmap;
  }

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State src = TargetResolver.staticTargetOf(obj.src, State.class);
    Slot func = TargetResolver.staticTargetOf(obj.eventFunc, Slot.class);
    List<Transition> list = get(src, func);
    list.add(obj);

    return null;
  }

}
