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

import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.Transition;
import ast.data.reference.RefFactory;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.specification.IsClass;

/**
 * Sets the destination of a transition to the initial substate if the destination is a composite state
 *
 * @author urs
 *
 */
public class TransitionRedirecter implements AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    TransitionRedirecterWorker redirecter = new TransitionRedirecterWorker();
    AstList<? extends Ast> hfsm = Collector.select(ast, new IsClass(ImplHfsm.class));
    redirecter.traverse(hfsm, null);
  }
}

class TransitionRedirecterWorker extends NullDispatcher<Void, Void> {
  static final private InitStateGetter initStateGetter = new InitStateGetter();

  @Override
  protected Void visitDefault(Ast obj, Void param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Void param) {
    visit(obj.topstate, null);
    return null;
  }

  @Override
  protected Void visitState(State obj, Void param) {
    visitList(obj.item, null);
    return null;
  }

  @Override
  protected Void visitTransition(Transition obj, Void param) {
    State dst = (State) obj.dst.getTarget();
    dst = initStateGetter.traverse(dst, null);
    obj.dst = RefFactory.create(obj.dst.metadata(), dst);
    return null;
  }

}
