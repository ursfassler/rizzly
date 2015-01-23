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

package fun.pass;

import java.util.HashSet;
import java.util.Set;

import pass.FunPass;
import util.SimpleGraph;
import fun.Fun;
import fun.doc.DepGraph;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Namespace;
import fun.type.base.BaseType;
import fun.variable.CompUse;

// FIXME if we remove everything unused, we can not typecheck that in EVL
public class UnusedRemover extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    FunList<CompUse> list = root.getItems(CompUse.class, false);
    assert (list.size() == 1);
    SimpleGraph<Fun> g = DepGraph.build(list.get(0));
    Set<Fun> keep = g.vertexSet();
    keep.addAll(root.getItems(BaseType.class, false));
    removeUnused(root, keep);
  }

  private static void removeUnused(Namespace ns, Set<Fun> keep) {
    Set<Fun> remove = new HashSet<Fun>();
    for (Fun itr : ns.getChildren()) {
      if (itr instanceof Namespace) {
        removeUnused((Namespace) itr, keep);
      } else if (!keep.contains(itr)) {
        remove.add(itr);
      }
    }
    ns.getChildren().removeAll(remove);
  }

}
