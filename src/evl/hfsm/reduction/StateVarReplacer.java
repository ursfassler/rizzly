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

import common.ElementInfo;

import evl.DefTraverser;
import evl.expression.reference.RefName;
import evl.expression.reference.Reference;
import evl.expression.reference.SimpleRef;
import evl.function.header.FuncPrivateRet;
import evl.function.header.FuncPrivateVoid;
import evl.hfsm.ImplHfsm;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.other.Namespace;
import evl.traverser.RefTypeGetter;
import evl.type.Type;
import evl.type.composed.NamedElement;
import evl.variable.Constant;
import evl.variable.StateVariable;
import evl.variable.Variable;

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
    for (ImplHfsm hfsm : evl.getItems(ImplHfsm.class, true)) {
      process(hfsm, kb);
    }
  }

  protected void process(ImplHfsm obj, KnowledgeBase kb) {
    StateTypeBuilder stb = new StateTypeBuilder();
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
    visit(obj.getTopstate(), param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.getItem(), param);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    visit(obj.getGuard(), param);
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateVoid(FuncPrivateVoid obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitFuncPrivateRet(FuncPrivateRet obj, Void param) {
    visit(obj.getBody(), param);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Void param) {
    if (obj.getLink() == dataVar) {
      visitList(obj.getOffset(), param);
      return null;
    }
    super.visitReference(obj, param);
    if (obj.getLink() instanceof StateVariable) {
      EvlList<NamedElement> eofs = epath.get(obj.getLink());
      assert (eofs != null);

      assert (obj.getOffset().isEmpty()); // FIXME not always true (e.g. for access to struct)

      Type type = dataVar.getType().getLink();

      obj.setLink(dataVar);
      for (NamedElement itr : eofs) {
        RefName ref = new RefName(ElementInfo.NO, itr.getName());

        type = rtg.traverse(ref, type);  // sanity check

        obj.getOffset().add(ref);
      }

    }
    return null;
  }

}
