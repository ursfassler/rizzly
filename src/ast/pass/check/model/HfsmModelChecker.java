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

package ast.pass.check.model;

import main.Configuration;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.SubCallbacks;
import ast.data.component.hfsm.ImplHfsm;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.Transition;
import ast.data.function.header.FuncFunction;
import ast.data.function.header.FuncProcedure;
import ast.data.function.header.FuncResponse;
import ast.data.function.header.FuncSlot;
import ast.data.variable.StateVariable;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;
import ast.repository.query.Collector;
import ast.specification.IsClass;
import error.ErrorType;
import error.RError;

//TODO check for unused states
//TODO check if a transition is never used
//TODO check that all queries are defined
//TODO check that no event is handled within a state
public class HfsmModelChecker extends AstPass {
  public HfsmModelChecker(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(Namespace ast, KnowledgeBase kb) {
    HfsmModelCheckerWorker check = new HfsmModelCheckerWorker();
    AstList<? extends Ast> hfsms = Collector.select(ast, new IsClass(ImplHfsm.class));
    check.traverse(hfsms, null);
  }
}

class HfsmModelCheckerWorker extends NullDispatcher<Void, Void> {

  @Override
  protected Void visitDefault(Ast obj, Void sym) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getSimpleName());
  }

  @Override
  protected Void visitSubCallbacks(SubCallbacks obj, Void param) {
    visitList(obj.func, param);
    return null;
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
    // TODO check that guard does not write state
    if (!(obj.eventFunc.getTarget() instanceof FuncSlot)) {
      RError.err(ErrorType.Error, obj.getInfo(), "transition can only be triggered by slot");
    }
    return null;
  }

  @Override
  protected Void visitFuncResponse(FuncResponse obj, Void param) {
    // TODO check that state is not written
    return null;
  }

  @Override
  protected Void visitFuncProcedure(FuncProcedure obj, Void param) {
    return null;
  }

  @Override
  protected Void visitFuncFunction(FuncFunction obj, Void param) {
    return null;
  }

  @Override
  protected Void visitStateVariable(StateVariable obj, Void param) {
    return null;
  }

}
