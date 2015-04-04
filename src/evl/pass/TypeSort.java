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

package evl.pass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import pass.EvlPass;
import util.Pair;
import util.SimpleGraph;
import evl.data.Namespace;
import evl.data.expression.reference.BaseRef;
import evl.data.type.Type;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;

public class TypeSort extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    List<Type> types = evl.getItems(Type.class, true);
    assert (evl.getChildren().containsAll(types));

    toposort(types);
    evl.getChildren().removeAll(types);
    evl.getChildren().addAll(types);
  }

  private static void toposort(List<Type> list) {
    SimpleGraph<Type> g = new SimpleGraph<Type>();
    for (Type u : list) {
      g.addVertex(u);
      Set<Type> vs = getDirectUsedTypes(u);
      for (Type v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<Type> old = new ArrayList<Type>(list);
    int size = list.size();
    list.clear();
    LinkedList<Type> nlist = new LinkedList<Type>();
    TopologicalOrderIterator<Type, Pair<Type, Type>> itr = new TopologicalOrderIterator<Type, Pair<Type, Type>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Type> diff = new ArrayList<Type>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<Type> getDirectUsedTypes(Type u) {
    DefTraverser<Void, Set<Type>> getter = new DefTraverser<Void, Set<Type>>() {
      @Override
      protected Void visitBaseRef(BaseRef obj, Set<Type> param) {
        if (obj.link instanceof Type) {
          param.add((Type) obj.link);
        }
        return null;
      }
    };
    Set<Type> vs = new HashSet<Type>();
    getter.traverse(u, vs);
    return vs;
  }
}
