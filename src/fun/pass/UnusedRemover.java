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

import pass.EvlPass;
import util.SimpleGraph;
import evl.data.Evl;
import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.component.composition.CompUse;
import evl.data.type.base.BaseType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ClassGetter;
import fun.doc.DepGraph;

// FIXME if we remove everything unused, we can not typecheck that in EVL
public class UnusedRemover extends EvlPass {

  @Override
  public void process(evl.data.Namespace root, KnowledgeBase kb) {
    EvlList<CompUse> list = ClassGetter.filter(CompUse.class, root.children);
    assert (list.size() == 1);
    SimpleGraph<Evl> g = DepGraph.build(list.get(0));
    Set<Evl> keep = g.vertexSet();
    keep.addAll(ClassGetter.filter(BaseType.class, root.children));
    removeUnused(root, keep);
  }

  private static void removeUnused(evl.data.Namespace ns, Set<Evl> keep) {
    Set<Evl> remove = new HashSet<Evl>();
    for (Evl itr : ns.children) {
      if (itr instanceof Namespace) {
        removeUnused((evl.data.Namespace) itr, keep);
      } else if (!keep.contains(itr)) {
        remove.add(itr);
      }
    }
    ns.children.removeAll(remove);
  }

}
