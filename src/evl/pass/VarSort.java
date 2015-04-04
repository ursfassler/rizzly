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
import evl.data.variable.Variable;
import evl.knowledge.KnowledgeBase;
import evl.traverser.DefTraverser;
import evl.traverser.other.ClassGetter;

//TODO merge with TypeSort?

public class VarSort extends EvlPass {

  @Override
  public void process(Namespace evl, KnowledgeBase kb) {
    List<Variable> vars = ClassGetter.filter(Variable.class, evl.children);
    assert (evl.children.containsAll(vars));

    toposortVar(vars);
    evl.children.removeAll(vars);
    evl.children.addAll(vars);
  }

  private static void toposortVar(List<Variable> list) {
    SimpleGraph<Variable> g = new SimpleGraph<Variable>();
    for (Variable u : list) {
      g.addVertex(u);
      Set<Variable> vs = getDirectUsedVariables(u);
      for (Variable v : vs) {
        g.addVertex(v);
        g.addEdge(u, v);
      }
    }

    ArrayList<Variable> old = new ArrayList<Variable>(list);
    int size = list.size();
    list.clear();
    LinkedList<Variable> nlist = new LinkedList<Variable>();
    TopologicalOrderIterator<Variable, Pair<Variable, Variable>> itr = new TopologicalOrderIterator<Variable, Pair<Variable, Variable>>(g);
    while (itr.hasNext()) {
      nlist.push(itr.next());
    }
    list.addAll(nlist);

    ArrayList<Variable> diff = new ArrayList<Variable>(list);
    diff.removeAll(old);
    old.removeAll(list);
    assert (size == list.size());
  }

  private static Set<Variable> getDirectUsedVariables(Variable u) {
    DefTraverser<Void, Set<Variable>> getter = new DefTraverser<Void, Set<Variable>>() {

      @Override
      protected Void visitBaseRef(BaseRef obj, Set<Variable> param) {
        if (obj.link instanceof Variable) {
          param.add((Variable) obj.link);
        }
        return super.visitBaseRef(obj, param);
      }
    };
    Set<Variable> vs = new HashSet<Variable>();
    getter.traverse(u, vs);
    return vs;
  }
}
