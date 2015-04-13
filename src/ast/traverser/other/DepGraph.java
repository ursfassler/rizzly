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

import java.util.Set;

import ast.data.Ast;
import ast.data.expression.reference.Reference;
import ast.data.expression.reference.SimpleRef;
import ast.data.function.Function;
import ast.doc.SimpleGraph;
import ast.traverser.DefTraverser;

public class DepGraph extends DefTraverser<Void, Ast> {
  final private SimpleGraph<Ast> g = new SimpleGraph<Ast>();

  static public SimpleGraph<Ast> build(Ast ast) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(ast, ast);
    return depGraph.g;
  }

  public static SimpleGraph<Ast> build(Set<? extends Ast> roots) {
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
  protected Void visitSimpleRef(SimpleRef obj, Ast param) {
    super.visitSimpleRef(obj, param);
    visit(obj.link, obj);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Ast param) {
    super.visitReference(obj, param);
    visit(obj.link, obj);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Ast param) {
    return super.visitFunction(obj, param);
  }

}
