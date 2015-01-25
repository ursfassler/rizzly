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

package evl.pass.infrastructure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;
import evl.function.Function;
import evl.hfsm.State;
import evl.hfsm.Transition;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.ImplElementary;
import evl.other.Namespace;
import evl.statement.VarDefStmt;
import evl.variable.ConstPrivate;
import evl.variable.StateVariable;
import evl.variable.Variable;

/**
 * Verifies that links to variables are ok, i.e. they are declared before use and in a visible scope.
 *
 * @author urs
 *
 */
public class VarLinkOk extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    VarLinkOkWorker worker = new VarLinkOkWorker(kb);
    worker.traverse(evl, new HashSet<Variable>());
  }

}

class VarLinkOkWorker extends DefTraverser<Void, Set<Variable>> {
  private final KnowParent kp;

  public VarLinkOkWorker(KnowledgeBase kb) {
    super();
    this.kp = kb.getEntry(KnowParent.class);
  }

  private Set<Variable> add(Set<Variable> param, Collection<? extends Variable> items) {
    param = new HashSet<Variable>(param);
    param.addAll(items);
    return param;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Set<Variable> param) {
    param = add(param, obj.getItems(Variable.class, false));
    return super.visitNamespace(obj, param);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<Variable> param) {
    param = new HashSet<Variable>(param);
    param.addAll(obj.getVariable());
    param.addAll(obj.getConstant());
    return super.visitImplElementary(obj, param);
  }

  @Override
  protected Void visitState(State obj, Set<Variable> param) {
    param = new HashSet<Variable>(param);
    param = add(param, obj.getItem().getItems(StateVariable.class));
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Set<Variable> param) {
    super.visitVarDef(obj, param);
    param.add(obj.getVariable());
    return null;
  }

  @Override
  protected Void visitFunctionImpl(Function obj, Set<Variable> param) {
    param = add(param, obj.getParam());
    return super.visitFunctionImpl(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Set<Variable> param) {
    if (obj.getLink() instanceof Variable) {
      if (!param.contains(obj.getLink())) {
        RError.err(ErrorType.Fatal, obj.getInfo(), "variable " + obj.getLink().toString() + " not visible from here");
      }
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, Set<Variable> param) {
    param = new HashSet<Variable>(param);
    param.addAll(obj.getParam());
    visit(obj.getBody(), param);
    addAllToTop(obj.getSrc().getLink(), param);
    visit(obj.getGuard(), param);
    return null;
  }

  private void addAllToTop(State state, Set<Variable> param) {
    while (true) {
      param.addAll(state.getItem().getItems(StateVariable.class));
      param.addAll(state.getItem().getItems(ConstPrivate.class));
      Evl parent = kp.getParent(state);
      if (parent instanceof State) {
        state = (State) parent;
      } else {
        return;
      }
    }
  }

}
