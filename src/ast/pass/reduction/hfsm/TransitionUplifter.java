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

import java.util.ArrayList;
import java.util.List;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateComposite;
import ast.data.component.hfsm.StateContent;
import ast.data.component.hfsm.Transition;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

/**
 * Moves all transitions of all states to the top-state.
 *
 * @author urs
 *
 */

public class TransitionUplifter extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, ast)) {
      TransitionUplifterWorker know = new TransitionUplifterWorker();
      know.traverse(hfsm, null);
    }
  }

}

class TransitionUplifterWorker extends NullTraverser<Void, List<Transition>> {

  @Override
  protected Void visitDefault(Ast obj, List<Transition> param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, List<Transition> param) {
    List<Transition> list = new ArrayList<Transition>();
    visit(obj.topstate, list);
    obj.topstate.item.addAll(list);
    return null;
  }

  @Override
  protected Void visitStateComposite(StateComposite obj, List<Transition> param) {
    visitList(obj.item, param);
    return null;
  }

  @Override
  protected Void visitState(State obj, List<Transition> param) {
    List<Transition> transList = ClassGetter.filter(Transition.class, obj.item);
    param.addAll(transList);
    obj.item.removeAll(transList);
    return super.visitState(obj, param);
  }

}
