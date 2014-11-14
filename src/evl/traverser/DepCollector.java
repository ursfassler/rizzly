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

package evl.traverser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import evl.DefTraverser;
import evl.Evl;
import evl.expression.reference.BaseRef;

public class DepCollector extends DefTraverser<Void, Void> {

  private Set<Evl> visited = new HashSet<Evl>();

  public static Set<Evl> process(Evl top) {
    DepCollector collector = new DepCollector();
    collector.traverse(top, null);
    return collector.visited;
  }

  public static Set<Evl> process(Collection<? extends Evl> pubfunc) {
    DepCollector collector = new DepCollector();
    for (Evl func : pubfunc) {
      collector.traverse(func, null);
    }
    return collector.visited;
  }

  @Override
  protected Void visit(Evl obj, Void param) {
    if (!visited.contains(obj)) {
      visited.add(obj);
      super.visit(obj, param);
    }
    return null;
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Void param) {
    super.visitBaseRef(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

}
