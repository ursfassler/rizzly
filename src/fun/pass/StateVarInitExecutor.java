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

package fun.pass;

import pass.EvlPass;
import evl.data.expression.Expression;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import fun.traverser.Memory;
import fun.traverser.spezializer.ExprEvaluator;

/**
 * Execute initial values of state variables (for static initialization)
 *
 * @author urs
 *
 */
public class StateVarInitExecutor extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    StateVarInitExecutorWorker worker = new StateVarInitExecutorWorker();
    worker.traverse(root, kb);
  }

}

class StateVarInitExecutorWorker extends DefTraverser<Void, KnowledgeBase> {

  @Override
  protected Void visitStateVariable(evl.data.variable.StateVariable obj, KnowledgeBase param) {
    Expression val = (Expression) ExprEvaluator.evaluate(obj.def, new Memory(), param);
    assert (val != null);
    obj.def = val;
    return null;
  }

}
