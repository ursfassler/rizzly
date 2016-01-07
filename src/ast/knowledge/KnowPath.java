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

package ast.knowledge;

import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.data.Ast;
import ast.data.Named;
import ast.dispatcher.DfsTraverser;
import error.ErrorType;
import error.RError;

public class KnowPath extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Ast, Designator> cache = new HashMap<Ast, Designator>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Designator get(Ast obj) {
    Designator ret = find(obj);
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  public Designator find(Ast obj) {
    Designator ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowPathTraverser traverser = new KnowPathTraverser(cache);
    traverser.traverse(base.getRoot(), new Designator());
  }

  public void clear() {
    cache.clear();
  }

}

class KnowPathTraverser extends DfsTraverser<Void, Designator> {
  private Map<Ast, Designator> cache;

  public KnowPathTraverser(Map<Ast, Designator> cache) {
    super();
    this.cache = cache;
  }

  @Override
  protected Void visit(Ast obj, Designator param) {
    if (obj instanceof Named) {
      if (cache.containsKey(obj)) {
        Designator oldparent = cache.get(obj);
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times: " + oldparent + " and " + param);
      }
      cache.put(obj, param);
      param = new Designator(param, ((Named) obj).name);
    }
    return super.visit(obj, param);
  }

}
