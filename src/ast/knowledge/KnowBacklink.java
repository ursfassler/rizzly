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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ast.data.Ast;
import ast.data.reference.Reference;
import ast.traverser.DefTraverser;
import error.ErrorType;
import error.RError;

public class KnowBacklink extends KnowledgeEntry {
  private KnowledgeBase kb;
  final private Map<Ast, Set<Reference>> cache = new HashMap<Ast, Set<Reference>>();

  @Override
  public void init(KnowledgeBase base) {
    kb = base;
  }

  public Set<Reference> get(Ast target) {
    Set<Reference> set = cache.get(target);
    if (set == null) {
      cache.clear();
      BacklinkTraverser traverser = new BacklinkTraverser();
      traverser.traverse(kb.getRoot(), cache);
      set = new HashSet<Reference>();
      if (set == null) {
        RError.err(ErrorType.Fatal, target.getInfo(), "Object not reachable:" + target);
      }
    }
    return set;
  }

}

class BacklinkTraverser extends DefTraverser<Void, Map<Ast, Set<Reference>>> {

  @Override
  protected Void visit(Ast obj, Map<Ast, Set<Reference>> param) {
    if (!param.containsKey(obj)) {
      param.put(obj, new HashSet<Reference>());
    }
    return super.visit(obj, param);
  }

  @Override
  protected Void visitReference(Reference obj, Map<Ast, Set<Reference>> param) {
    Set<Reference> set = param.get(obj.link);
    if (set == null) {
      set = new HashSet<Reference>();
      param.put(obj.link, set);
    }
    set.add(obj);
    return super.visitReference(obj, param);
  }

}
