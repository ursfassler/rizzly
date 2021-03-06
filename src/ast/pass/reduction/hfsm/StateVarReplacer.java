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
import java.util.Map;

import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.data.expression.ReferenceExpression;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.Procedure;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefFactory;
import ast.data.reference.RefName;
import ast.data.type.Type;
import ast.data.type.TypeRefFactory;
import ast.data.type.composed.NamedElement;
import ast.data.variable.Constant;
import ast.data.variable.StateVariable;
import ast.data.variable.Variable;
import ast.dispatcher.DfsTraverser;
import ast.dispatcher.other.RefTypeGetter;
import ast.knowledge.KnowType;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.specification.IsClass;

//TODO set correct values when switching states

/**
 * Introduces types and constants for states and their variables. Relinkes access to variables to access record
 * elements.
 *
 * @author urs
 *
 */
public class StateVarReplacer implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : Collector.select(ast, new IsClass(ImplHfsm.class)).castTo(ImplHfsm.class)) {
      process(hfsm, kb);
    }
  }

  protected void process(ImplHfsm obj, KnowledgeBase kb) {
    KnowType kt = kb.getEntry(KnowType.class);

    StateTypeBuilder stb = new StateTypeBuilder(kb);
    NamedElement elem = stb.traverse(obj.topstate, new AstList<NamedElement>());
    Type stateType = kt.get(elem.typeref);

    obj.topstate.initial = RefFactory.create(InitStateGetter.get(obj.topstate));

    Constant def = stb.getInitVar().get(stateType);

    // TODO set correct values when switching states
    StateVariable var = new StateVariable("data", TypeRefFactory.create(stateType), new ReferenceExpression(RefFactory.withOffset(def)));
    obj.topstate.item.add(var);

    Map<StateVariable, AstList<NamedElement>> epath = new HashMap<StateVariable, AstList<NamedElement>>(stb.getEpath());
    for (StateVariable sv : epath.keySet()) {
      epath.get(sv).remove(0);
    }

    StateVarReplacerWorker replacer = new StateVarReplacerWorker(var, epath, kb);
    replacer.traverse(obj, null);
  }
}

class StateVarReplacerWorker extends DfsTraverser<Void, Void> {
  final private Variable dataVar;
  final private Map<StateVariable, AstList<NamedElement>> epath;
  final private RefTypeGetter rtg;
  final private KnowType kt;

  public StateVarReplacerWorker(Variable dataVar, Map<StateVariable, AstList<NamedElement>> epath, KnowledgeBase kb) {
    super();
    this.epath = epath;
    this.dataVar = dataVar;
    kt = kb.getEntry(KnowType.class);
    rtg = new RefTypeGetter(kt, kb);
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
  protected Void visitFuncProcedure(Procedure obj, Void param) {
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Void param) {
    visit(obj.body, param);
    return null;
  }

  @Override
  protected Void visitOffsetReference(OffsetReference obj, Void param) {
    LinkedAnchor anchor = (LinkedAnchor) obj.getAnchor();

    if (anchor.getLink() == dataVar) {
      visitList(obj.getOffset(), param);
      return null;
    }
    super.visitOffsetReference(obj, param);
    if (anchor.getLink() instanceof StateVariable) {
      AstList<NamedElement> eofs = epath.get(anchor.getLink());
      assert (eofs != null);

      assert (obj.getOffset().isEmpty()); // FIXME not always true (e.g. for access
      // to struct)

      Type type = kt.get(dataVar.type);

      anchor.setLink(dataVar);
      for (NamedElement itr : eofs) {
        RefName ref = new RefName(itr.getName());

        type = rtg.traverse(ref, type); // sanity check

        obj.getOffset().add(ref);
      }

    }
    return null;
  }

}
