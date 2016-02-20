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

package ast.dispatcher.other;

import java.util.Collection;

import ast.data.Ast;
import ast.data.function.Function;
import ast.data.reference.LinkedAnchor;
import ast.data.reference.UnlinkedAnchor;
import ast.dispatcher.DfsTraverser;
import ast.doc.SimpleGraph;

public class DepGraph extends DfsTraverser<Void, Ast> {
  final private SimpleGraph<Ast> g = new SimpleGraph<Ast>();

  static public SimpleGraph<Ast> build(Ast ast) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(ast, ast);
    return depGraph.g;
  }

  public static SimpleGraph<Ast> build(Collection<? extends Ast> roots) {
    DepGraph depGraph = new DepGraph();
    for (Ast itr : roots) {
      depGraph.traverse(itr, itr);
    }
    return depGraph.g;
  }

  @Override
  protected Void visit(Ast obj, Ast param) {
    boolean visited = g.containsVertex(obj);
    g.addVertex(obj);
    g.addEdge(param, obj);
    if (visited) {
      return null;
    }
    return super.visit(obj, obj);
  }

  @Override
  protected Void visitFunction(Function obj, Ast param) {
    return super.visitFunction(obj, param);
  }

  @Override
  protected Void visitLinkedAnchor(LinkedAnchor obj, Ast param) {
    super.visitLinkedAnchor(obj, param);
    visit(obj.getLink(), param);
    return null;
  }

  @Override
  protected Void visitUnlinkedAnchor(UnlinkedAnchor obj, Ast param) {
    throw new RuntimeException("not yet implemented");
  }

}
