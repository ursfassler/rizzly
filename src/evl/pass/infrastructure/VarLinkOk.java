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
import evl.data.Evl;
import evl.data.Namespace;
import evl.data.component.composition.SubCallbacks;
import evl.data.component.elementary.ImplElementary;
import evl.data.component.hfsm.State;
import evl.data.component.hfsm.Transition;
import evl.data.expression.reference.BaseRef;
import evl.data.function.Function;
import evl.data.statement.ForStmt;
import evl.data.statement.VarDefStmt;
import evl.data.type.Type;
import evl.data.variable.ConstPrivate;
import evl.data.variable.StateVariable;
import evl.data.variable.Variable;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

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
    worker.traverse(evl, new HashSet<Evl>());
  }

}

class VarLinkOkWorker extends DefTraverser<Void, Set<Evl>> {
  private final KnowParent kp;

  public VarLinkOkWorker(KnowledgeBase kb) {
    super();
    this.kp = kb.getEntry(KnowParent.class);
  }

  private Set<Evl> add(Set<Evl> param, Collection<? extends Evl> items) {
    param = new HashSet<Evl>(param);
    param.addAll(items);
    return param;
  }

  @Override
  protected Void visitNamespace(Namespace obj, Set<Evl> param) {
    param = add(param, obj.getItems(Variable.class, false));
    param = add(param, obj.getItems(Type.class, false));
    return super.visitNamespace(obj, param);
  }

  @Override
  protected Void visitImplElementary(ImplElementary obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param.addAll(obj.variable);
    param.addAll(obj.constant);
    param.addAll(obj.type);
    return super.visitImplElementary(obj, param);
  }

  @Override
  protected Void visitState(State obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param = add(param, obj.item.getItems(StateVariable.class));
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Set<Evl> param) {
    super.visitVarDef(obj, param);
    param.add(obj.variable);
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param.add(obj.iterator);
    super.visitForStmt(obj, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Set<Evl> param) {
    param = add(param, obj.param);
    return super.visitFunction(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Set<Evl> param) {
    if ((obj.link instanceof Variable) || (obj.link instanceof Type)) {
      if (!param.contains(obj.link)) {
        RError.err(ErrorType.Fatal, obj.getInfo(), "object " + obj.link.toString() + " not visible from here");
      }
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param.addAll(obj.param);
    visit(obj.body, param);
    addAllToTop(obj.src.link, param);
    visit(obj.guard, param);
    return null;
  }

  private void addAllToTop(State state, Set<Evl> param) {
    while (true) {
      param.addAll(state.item.getItems(StateVariable.class));
      param.addAll(state.item.getItems(ConstPrivate.class));
      Evl parent = kp.getParent(state);
      if (parent instanceof State) {
        state = (State) parent;
      } else {
        return;
      }
    }
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Set<Evl> param) {
    return super.visitSubCallbacks(obj, param);
  }

}
