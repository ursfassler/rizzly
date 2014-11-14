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

package fun.knowledge;

import java.util.HashMap;
import java.util.Map;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;

public class KnowParent extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Fun, Fun> cache = new HashMap<Fun, Fun>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Fun get(Fun obj) {
    Fun ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public Fun find(Fun obj) {
    Fun ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowParentTraverser traverser = new KnowParentTraverser(cache);
    traverser.traverse(base.getRoot(), null);
  }

  public void clear() {
    cache.clear();
  }

}

class KnowParentTraverser extends DefTraverser<Void, Fun> {
  private Map<Fun, Fun> cache;

  public KnowParentTraverser(Map<Fun, Fun> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visit(Fun obj, Fun param) {
    cache.put(obj, param);
    return super.visit(obj, obj);
  }

}
