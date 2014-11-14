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

package evl.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import error.ErrorType;
import error.RError;
import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;
import evl.other.Named;

public class KnowBacklink extends KnowledgeEntry {
  private KnowledgeBase kb;
  final private Map<Evl, Set<BaseRef<Named>>> cache = new HashMap<Evl, Set<BaseRef<Named>>>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Set<BaseRef<Named>> get(Evl target) {
    Set<BaseRef<Named>> set = cache.get(target);
    if (set == null) {
      cache.clear();
      BacklinkTraverser traverser = new BacklinkTraverser();
      traverser.traverse(kb.getRoot(), cache);
      set = new HashSet<BaseRef<Named>>();
      if (set == null) {
        RError.err(ErrorType.Fatal, target.getInfo(), "Object not reachable:" + target);
      }
    }
    return set;
  }

}

class BacklinkTraverser extends DefTraverser<Void, Map<Evl, Set<BaseRef<Named>>>> {

  @Override
  protected Void visit(Evl obj, Map<Evl, Set<BaseRef<Named>>> param) {
    if (!param.containsKey(obj)) {
      param.put(obj, new HashSet<BaseRef<Named>>());
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<Evl, Set<BaseRef<Named>>> param) {
    Set<BaseRef<Named>> set = param.get(obj.getLink());
    if (set == null) {
      set = new HashSet<BaseRef<Named>>();
      param.put(obj.getLink(), set);
    }
    set.add(obj);
    return super.visitBaseRef(obj, param);
  }

}
