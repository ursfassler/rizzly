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

package evl.pass.sanitycheck;

import java.util.HashSet;
import java.util.Set;

import pass.EvlPass;
import error.ErrorType;
import error.RError;
import evl.data.Evl;
import evl.data.Namespace;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

/**
 * Checks that a object has only one parent
 *
 * @author urs
 *
 */
public class SingleDefinition extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    SingleDefinitionWorker worker = new SingleDefinitionWorker();
    worker.traverse(evl, new HashSet<Evl>());
  }
}

class SingleDefinitionWorker extends DefTraverser<Void, Set<Evl>> {

  @Override
  protected Void visit(Evl obj, Set<Evl> param) {
    if (param.contains(obj)) {
      for (Object itr : param.toArray()) {
        if (obj == itr) {
          RError.err(ErrorType.Fatal, obj.getInfo(), "object defined at 2 places: " + obj);
        }
      }
    }
    param.add(obj);
    return super.visit(obj, param);
  }
}
