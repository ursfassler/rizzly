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

package ast.pass;

import java.util.HashSet;
import java.util.Set;

import pass.AstPass;
import util.SimpleGraph;
import ast.data.Ast;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.component.composition.CompUse;
import ast.data.type.base.BaseType;
import ast.doc.DepGraph;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ClassGetter;

// FIXME if we remove everything unused, we can not typecheck that in EVL
public class UnusedRemover extends AstPass {

  @Override
  public void process(ast.data.Namespace root, KnowledgeBase kb) {
    AstList<CompUse> list = ClassGetter.filter(CompUse.class, root.children);
    assert (list.size() == 1);
    SimpleGraph<Ast> g = DepGraph.build(list.get(0));
    Set<Ast> keep = g.vertexSet();
    keep.addAll(ClassGetter.filter(BaseType.class, root.children));
    removeUnused(root, keep);
  }

  private static void removeUnused(ast.data.Namespace ns, Set<Ast> keep) {
    Set<Ast> remove = new HashSet<Ast>();
    for (Ast itr : ns.children) {
      if (itr instanceof Namespace) {
        removeUnused((ast.data.Namespace) itr, keep);
      } else if (!keep.contains(itr)) {
        remove.add(itr);
      }
    }
    ns.children.removeAll(remove);
  }

}
