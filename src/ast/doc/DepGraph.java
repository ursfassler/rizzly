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

package ast.doc;

import ast.data.Ast;
import ast.data.reference.Reference;
import ast.dispatcher.DfsTraverser;

public class DepGraph extends DfsTraverser<Void, Ast> {
  final private SimpleGraph<Ast> g = new SimpleGraph<Ast>();

  static public SimpleGraph<Ast> build(Ast root) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(root, root);
    return depGraph.g;
  }

  public SimpleGraph<Ast> getGraph() {
    return g;
  }

  @Override
  protected Void visit(Ast obj, Ast param) {
    boolean visited = g.containsVertex(obj);
    if (!visited) {
      g.addVertex(obj);
    }
    g.addEdge(param, obj);
    if (!visited) {
      super.visit(obj, obj);
    }
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Ast param) {
    super.visitReference(obj, param);
    visit(obj.link, obj);
    return null;
  }

}
