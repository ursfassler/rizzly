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

package evl.pass;

import pass.EvlPass;
import evl.data.Namespace;
import evl.data.expression.Expression;
import evl.data.expression.TupleValue;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ExprReplacer;

public class ReduceTuple extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    ReduceTupleWorker worker = new ReduceTupleWorker();
    worker.traverse(evl, null);
  }

}

class ReduceTupleWorker extends ExprReplacer<Void> {

  @Override
  protected Expression visitTupleValue(TupleValue obj, Void param) {
    if (obj.value.size() == 1) {
      return visit(obj.value.get(0), param);
    } else {
      return super.visitTupleValue(obj, param);
    }
  }

}
