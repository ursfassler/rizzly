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

package ast.pass.hfsmreduction;

import java.util.ArrayList;
import java.util.List;

import ast.Designator;
import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.type.Type;
import ast.data.variable.ConstPrivate;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.traverser.NullTraverser;
import ast.traverser.other.ClassGetter;

/**
 * Moves items of all states to the top-state.
 *
 * @author urs
 *
 */
public class StateItemUplifter extends AstPass {

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    for (ImplHfsm hfsm : ClassGetter.getRecursive(ImplHfsm.class, ast)) {
      StateItemUplifterWorker know = new StateItemUplifterWorker(kb);
      know.traverse(hfsm, null);
    }
  }
}

class StateItemUplifterWorker extends NullTraverser<Void, Designator> {
  final private List<StateContent> func = new ArrayList<StateContent>();

  public StateItemUplifterWorker(KnowledgeBase kb) {
    super();
  }

  @Override
  protected Void visitDefault(Ast obj, Designator param) {
    if (obj instanceof StateContent) {
      return null;
    } else {
      throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
    }
  }

  @Override
  protected Void visitImplHfsm(ImplHfsm obj, Designator param) {
    visit(obj.topstate, new Designator());
    obj.topstate.item.addAll(func);
    return null;
  }

  @Override
  protected Void visitState(State obj, Designator param) {
    param = new Designator(param, obj.name);
    // visit(obj.getEntryCode(), param);//TODO correct? It is no longer a
    // function and should not exist at this point
    // visit(obj.getExitCode(), param);
    visitList(obj.item, param);

    obj.item.removeAll(func);

    return null;
  }

  @Override
  protected Void visitFuncProcedure(FuncProcedure obj, Designator param) {
    param = new Designator(param, obj.name);
    obj.name = param.toString(Designator.NAME_SEP);
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Designator param) {
    param = new Designator(param, obj.name);
    obj.name = param.toString(Designator.NAME_SEP);
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitType(Type obj, Designator param) {
    param = new Designator(param, obj.name);
    obj.name = param.toString(Designator.NAME_SEP);
    func.add(obj);
    return null;
  }

  @Override
  protected Void visitConstPrivate(ConstPrivate obj, Designator param) {
    param = new Designator(param, obj.name);
    obj.name = param.toString(Designator.NAME_SEP);
    func.add(obj);
    return null;
  }

}
