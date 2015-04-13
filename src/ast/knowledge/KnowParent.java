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
import java.util.LinkedList;
import java.util.Map;

import ast.Designator;
import ast.data.Ast;
import ast.data.type.base.EnumElement;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

/**
 * Knows the parent of most objects
 */
public class KnowParent extends KnowledgeEntry {
  private KnowledgeBase base;
  private Map<Ast, Ast> cache = new HashMap<Ast, Ast>();

  @Override
  public void init(KnowledgeBase base) {
    this.base = base;
  }

  public Ast get(Ast obj) {
    Ast ret = cache.get(obj);
    if (ret == null) {
      rebuild();
      ret = cache.get(obj);
    }
    if (ret == null) {
      RError.err(ErrorType.Fatal, obj.getInfo(), "Object not reachable: " + obj);
    }
    return ret;
  }

  private void rebuild() {
    cache.clear();
    KnowParentTraverser traverser = new KnowParentTraverser(cache);
    traverser.traverse(base.getRoot(), null);
  }

}

class KnowParentTraverser extends DefTraverser<Void, Ast> {
  private Map<Ast, Ast> cache;

  public KnowParentTraverser(Map<Ast, Ast> cache) {
    super();
    this.cache = cache;
  }

  private Designator getPath(Ast obj) {
    LinkedList<String> name = new LinkedList<String>();
    while (obj != null) {
      name.push(obj.toString());
      obj = cache.get(obj);
    }
    return new Designator(name);
  }

  @Override
  protected Void visit(Ast obj, Ast param) {
    assert (obj != param);
    if (cache.containsKey(obj)) {
      if (!(obj instanceof EnumElement)) { // FIXME remove this hack (new enum
        // type system?)
        Ast oldparent = cache.get(obj);
        RError.err(ErrorType.Hint, "First time was here:  " + getPath(oldparent));
        RError.err(ErrorType.Hint, "Second time was here: " + getPath(param));
        RError.err(ErrorType.Fatal, obj.getInfo(), "Same object (" + obj + ") found 2 times");
      }
    }
    cache.put(obj, param);
    return super.visit(obj, obj);
  }

}
