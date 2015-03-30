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
import evl.other.SubCallbacks;
import evl.statement.ForStmt;
import evl.statement.VarDefStmt;
import evl.type.Type;
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
    param.addAll(obj.getVariable());
    param.addAll(obj.getConstant());
    param.addAll(obj.getType());
    return super.visitImplElementary(obj, param);
  }

  @Override
  protected Void visitState(State obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param = add(param, obj.getItem().getItems(StateVariable.class));
    return super.visitState(obj, param);
  }

  @Override
  protected Void visitVarDef(VarDefStmt obj, Set<Evl> param) {
    super.visitVarDef(obj, param);
    param.add(obj.getVariable());
    return null;
  }

  @Override
  protected Void visitForStmt(ForStmt obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param.add(obj.getIterator());
    super.visitForStmt(obj, param);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Set<Evl> param) {
    param = add(param, obj.getParam());
    return super.visitFunction(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Set<Evl> param) {
    if ((obj.getLink() instanceof Variable) || (obj.getLink() instanceof Type)) {
      if (!param.contains(obj.getLink())) {
        RError.err(ErrorType.Fatal, obj.getInfo(), "object " + obj.getLink().toString() + " not visible from here");
      }
    }
    return super.visitBaseRef(obj, param);
  }

  @Override
  protected Void visitTransition(Transition obj, Set<Evl> param) {
    param = new HashSet<Evl>(param);
    param.addAll(obj.getParam());
    visit(obj.getBody(), param);
    addAllToTop(obj.getSrc().getLink(), param);
    visit(obj.getGuard(), param);
    return null;
  }

  private void addAllToTop(State state, Set<Evl> param) {
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

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Set<Evl> param) {
    return super.visitSubCallbacks(obj, param);
  }

}
