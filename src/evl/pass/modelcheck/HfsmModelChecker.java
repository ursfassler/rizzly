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

package evl.pass.modelcheck;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.Transition;
import evl.data.function.header.FuncFunction;
import evl.data.function.header.FuncProcedure;
import evl.data.function.header.FuncResponse;
import evl.data.function.header.FuncSlot;
import evl.data.variable.StateVariable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.NullTraverser;
import evl.traverser.other.ClassGetter;

//TODO check for unused states
//TODO check if a transition is never used
//TODO check that all queries are defined
//TODO check that no event is handled within a state
public class HfsmModelChecker extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    EvlList<ImplHfsm> hfsms = ClassGetter.getRecursive(ImplHfsm.class, evl);
    HfsmModelCheckerWorker check = new HfsmModelCheckerWorker();
    for (ImplHfsm hfsm : hfsms) {
      check.traverse(hfsm, null);
    }
  }
}

class HfsmModelCheckerWorker extends NullTraverser<Void, Void> {

  @Override
  protected Void visitDefault(Evl obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    visitList(obj.func, param);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.topstate, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    // TODO check that guard does not write state
    if (!(obj.eventFunc.getTarget() instanceof FuncSlot)) {
      RError.err(ErrorType.Error, obj.getInfo(), "transition can only be triggered by slot");
    }
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Void param) {
    // TODO check that state is not written
    return null;
  }

  @Override
  protected Void visitFuncProcedure(FuncProcedure obj, Void param) {
    return null;
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    return null;
  }

}
