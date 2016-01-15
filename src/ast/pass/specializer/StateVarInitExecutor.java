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

package ast.pass.specializer;

import main.Configuration;
import ast.data.variable.StateVariable;
import ast.dispatcher.DfsTraverser;
import ast.interpreter.Memory;
import ast.knowledge.KnowledgeBase;
import ast.pass.AstPass;

/**
 * Execute initial values of state variables (for static initialization)
 *
 * @author urs
 *
 */
public class StateVarInitExecutor extends AstPass {
  public StateVarInitExecutor(Configuration configuration) {
    super(configuration);
  }

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    StateVarInitExecutorWorker worker = new StateVarInitExecutorWorker();
    worker.traverse(root, kb);
  }

}

class StateVarInitExecutorWorker extends DfsTraverser<Void, KnowledgeBase> {

  @Override
  protected Void visitStateVariable(StateVariable obj, KnowledgeBase param) {
    obj.def = ExprEvaluator.evaluate(obj.def, new Memory(), param);
    return null;
  }

}
