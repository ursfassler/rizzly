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

import java.util.HashMap;
import java.util.Map;

import pass.EvlPass;
import evl.DefTraverser;
import evl.Evl;
import evl.knowledge.KnowledgeBase;
import evl.other.Namespace;

public class Count extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    Map<Class<? extends Evl>, Integer> map = new HashMap<Class<? extends Evl>, Integer>();
    CountWorker worker = new CountWorker();
    worker.traverse(evl, map);
    for (Class<? extends Evl> cl : map.keySet()) {
      System.out.println(map.get(cl) + "\t" + cl.getName());
    }
  }
}

class CountWorker extends DefTraverser<Void, Map<Class<? extends Evl>, Integer>> {

  @Override
  protected Void visit(Evl obj, Map<Class<? extends Evl>, Integer> param) {
    int val;
    if (param.containsKey(obj.getClass())) {
      val = param.get(obj.getClass());
    } else {
      val = 0;
    }
    val++;
    param.put(obj.getClass(), val);
    return super.visit(obj, param);
  }

}
