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

package fun.doc;

import util.SimpleGraph;
import fun.DefTraverser;
import fun.Fun;
import fun.expression.reference.BaseRef;

public class DepGraph extends DefTraverser<Void, Fun> {
  final private SimpleGraph<Fun> g = new SimpleGraph<Fun>();

  static public SimpleGraph<Fun> build(Fun root) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(root, root);
    return depGraph.g;
  }

  public SimpleGraph<Fun> getGraph() {
    return g;
  }

  @Override
  protected Void visit(Fun obj, Fun param) {
    boolean visited = g.containsVertex(obj);
    g.addVertex(obj);
    g.addEdge(param, obj);
    if (visited) {
      return null;
    }
    return super.visit(obj, obj);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Fun param) {
    super.visitBaseRef(obj, param);
    visit(obj.getLink(), obj);
    return null;
  }

}
