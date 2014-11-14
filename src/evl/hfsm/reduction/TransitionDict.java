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

package evl.hfsm.reduction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import evl.Evl;
import evl.NullTraverser;
import evl.function.header.FuncCtrlInDataIn;
import evl.hfsm.State;
import evl.hfsm.StateItem;
import evl.hfsm.Transition;
import evl.other.EvlList;

public class TransitionDict extends NullTraverser<Void, Void> {
  private Map<State, Map<FuncCtrlInDataIn, EvlList<Transition>>> transition = new HashMap<State, Map<FuncCtrlInDataIn, EvlList<Transition>>>();

  public EvlList<Transition> get(State src, FuncCtrlInDataIn func) {
    Map<FuncCtrlInDataIn, EvlList<Transition>> smap = transition.get(src);
    if (smap == null) {
      smap = new HashMap<FuncCtrlInDataIn, EvlList<Transition>>();
      transition.put(src, smap);
    }
    EvlList<Transition> fmap = smap.get(func);
    if (fmap == null) {
      fmap = new EvlList<Transition>();
      smap.put(func, fmap);
    }
    return fmap;
  }

  @Override
  protected Void visitDefault(Evl obj, Void param) {
    if (obj instanceof StateItem) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State src = obj.getSrc().getLink();
    FuncCtrlInDataIn func = obj.getEventFunc().getLink();
    List<Transition> list = get(src, func);
    list.add(obj);

    return null;
  }

}
