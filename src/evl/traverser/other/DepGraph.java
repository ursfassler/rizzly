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

package evl.traverser.other;

import java.util.Set;

import util.SimpleGraph;
import evl.data.Evl;
import evl.data.expression.reference.Reference;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.traverser.DefTraverser;

public class DepGraph extends DefTraverser<Void, Evl> {
  final private SimpleGraph<Evl> g = new SimpleGraph<Evl>();

  static public SimpleGraph<Evl> build(Evl evl) {
    DepGraph depGraph = new DepGraph();
    depGraph.traverse(evl, evl);
    return depGraph.g;
  }

  public static SimpleGraph<Evl> build(Set<? extends Evl> roots) {
    DepGraph depGraph = new DepGraph();
    for (Evl itr : roots) {
      depGraph.traverse(itr, itr);
    }
    return depGraph.g;
  }

  @Override
  protected Void visit(Evl obj, Evl param) {
    boolean visited = g.containsVertex(obj);
    g.addVertex(obj);
    g.addEdge(param, obj);
    if (visited) {
      return null;
    }
    return super.visit(obj, obj);
  }

  @Override
  protected Void visitSimpleRef(SimpleRef obj, Evl param) {
    super.visitSimpleRef(obj, param);
    visit(obj.link, obj);
    return null;
  }

  @Override
  protected Void visitReference(Reference obj, Evl param) {
    super.visitReference(obj, param);
    visit(obj.link, obj);
    return null;
  }

  @Override
  protected Void visitFunction(Function obj, Evl param) {
    return super.visitFunction(obj, param);
  }

}
