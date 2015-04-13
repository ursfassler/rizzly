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

package ast.traverser.other;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ast.data.Ast;
import ast.data.expression.reference.BaseRef;
import ast.traverser.DefTraverser;

public class DepCollector extends DefTraverser<Void, Void> {

  private Set<Ast> visited = new HashSet<Ast>();

  public static Set<Ast> process(Ast top) {
    DepCollector collector = new DepCollector();
    collector.traverse(top, null);
    return collector.visited;
  }

  public static Set<Ast> process(Collection<? extends Ast> pubfunc) {
    DepCollector collector = new DepCollector();
    for (Ast func : pubfunc) {
      collector.traverse(func, null);
    }
    return collector.visited;
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    if (!visited.contains(obj)) {
      visited.add(obj);
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    visit(obj.link, param);
    return null;
  }

}
