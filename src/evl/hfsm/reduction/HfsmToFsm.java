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
import java.util.Map;

import pass.EvlPass;

import common.Designator;
import common.ElementInfo;

import evl.Evl;
import evl.NullTraverser;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.pass.OpenReplace;
import evl.type.Type;
import evl.type.composed.NamedElement;
import evl.variable.Constant;
import evl.variable.StateVariable;

//TODO set correct values when switching states

public class HfsmToFsm extends EvlPass {

  {
    addDependency(OpenReplace.class);
  }

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    HfsmToFsmWorker reduction = new HfsmToFsmWorker(kb);
    reduction.traverse(evl, null);
  }

}

class HfsmToFsmWorker extends NullTraverser<Void, Namespace> {
  KnowledgeBase kb;

  public HfsmToFsmWorker(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  @Override
  protected Void visitDefault(Evl obj, Namespace param) {
    return null;
  }

  @Override
  protected Void visitList(EvlList<? extends Evl> list, Namespace param) {
    // against comodification error
    EvlList<Evl> old = new EvlList<Evl>();
    for (Evl itr : list) {
      old.add(itr);
    }
    return super.visitList(old, param);
  }

  @Override
  protected Void visitNamespace(Namespace obj, Namespace param) {
    visitList(obj.getChildren(), obj);
    return null;
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Namespace param) {
    // TODO make clean namespace-tree for hfsm types and constants
    Namespace fsmSpace = param.force(Designator.NAME_SEP + obj.getName());

    QueryDownPropagator.process(obj, kb);

    TransitionRedirecter.process(obj.getTopstate());
    TransitionDownPropagator.process(obj, kb);

    {
      StateTypeBuilder stb = new StateTypeBuilder(fsmSpace, kb);
      NamedElement elem = stb.traverse(obj.getTopstate(), new EvlList<NamedElement>());
      Type stateType = elem.getRef().getLink();

      obj.getTopstate().setInitial(new SimpleRef<State>(ElementInfo.NO, InitStateGetter.get(obj.getTopstate())));

      Constant def = stb.getInitVar().get(stateType);

      // TODO set correct values when switching states
      StateVariable var = new StateVariable(ElementInfo.NO, "data", new SimpleRef<Type>(ElementInfo.NO, stateType), new Reference(ElementInfo.NO, def));
      obj.getTopstate().getItem().add(var);

      Map<StateVariable, EvlList<NamedElement>> epath = new HashMap<StateVariable, EvlList<NamedElement>>(stb.getEpath());
      for (StateVariable sv : epath.keySet()) {
        epath.get(sv).remove(0);
      }

      StateVarReplacer.process(obj, var, epath, kb);
    }

    EntryExitUpdater.process(obj, kb);
    StateFuncUplifter.process(obj, kb);
    TransitionUplifter.process(obj);
    LeafStateUplifter.process(obj, kb);

    return null;
  }

}
