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

package evl.pass.hfsm.reduction;

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.hfsm.ImplHfsm;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.Transition;
import evl.data.expression.reference.RefName;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.header.FuncPrivateRet;
import evl.data.function.header.FuncPrivateVoid;
import evl.data.type.Type;
import evl.data.type.composed.NamedElement;
import evl.data.variable.Constant;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ClassGetter;
import evl.traverser.other.RefTypeGetter;

//TODO set correct values when switching states

/**
 * Introduces types and constants for states and their variables. Relinkes access to variables to access record
 * elements.
 *
 * @author urs
 *
 */
public class StateVarReplacer extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, evl)) {
      process(hfsm, kb);
    }
  }

  protected void process(ImplHfsm obj, KnowledgeBase kb) {
    StateTypeBuilder stb = new StateTypeBuilder();
    NamedElement elem = stb.traverse(obj.topstate, new EvlList<NamedElement>());
    Type stateType = elem.ref.link;

    obj.topstate.initial = new SimpleRef<State>(ElementInfo.NO, InitStateGetter.get(obj.topstate));

    Constant def = stb.getInitVar().get(stateType);

    // TODO set correct values when switching states
    StateVariable var = new StateVariable(ElementInfo.NO, "data", new SimpleRef<Type>(ElementInfo.NO, stateType), new Reference(ElementInfo.NO, def));
    obj.topstate.item.add(var);

    Map<StateVariable, EvlList<NamedElement>> epath = new HashMap<StateVariable, EvlList<NamedElement>>(stb.getEpath());
    for (StateVariable sv : epath.keySet()) {
      epath.get(sv).remove(0);
    }

    StateVarReplacerWorker replacer = new StateVarReplacerWorker(var, epath, kb);
    replacer.traverse(obj, null);
  }
}

class StateVarReplacerWorker extends DefTraverser<Void, Void> {
  final private Variable dataVar;
  final private Map<StateVariable, EvlList<NamedElement>> epath;
  final private RefTypeGetter rtg;

  public StateVarReplacerWorker(Variable dataVar, Map<StateVariable, EvlList<NamedElement>> epath, KnowledgeBase kb) {
    super();
    this.epath = epath;
    this.dataVar = dataVar;
    rtg = new RefTypeGetter(kb);
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
    visit(obj.guard, param);
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.link == dataVar) {
      visitList(obj.offset, param);
      return null;
    }
    super.visitReference(obj, param);
    if (obj.link instanceof StateVariable) {
      EvlList<NamedElement> eofs = epath.get(obj.link);
      assert (eofs != null);

      assert (obj.offset.isEmpty()); // FIXME not always true (e.g. for access to struct)

      Type type = dataVar.type.link;

      obj.link = dataVar;
      for (NamedElement itr : eofs) {
        RefName ref = new RefName(ElementInfo.NO, itr.name);

        type = rtg.traverse(ref, type);  // sanity check

        obj.offset.add(ref);
      }

    }
    return null;
  }

}
