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

package ast.manipulator;

import ast.data.Ast;
import ast.data.AstList;
import ast.knowledge.KnowledgeBase;
import ast.repository.ChildCollector;
import ast.repository.NameFilter;
import ast.repository.Single;
import ast.specification.Equals;

public class RepoAdder {
  final private KnowledgeBase kb;

  public RepoAdder(KnowledgeBase kb) {
    super();
    this.kb = kb;
  }

  public Ast find(String name) {
    return NameFilter.select(kb.getRoot().children, name);
  }

  public <T extends Ast> T replaceOrRegister(T at) {
    AstList<Ast> matches = findMatch(at);
    Ast found = Single.first(matches);

    if (found == null) {
      add(at);
      return at;
    } else {
      return (T) found;
    }
  }

  private AstList<Ast> findMatch(Ast item) {
    return ChildCollector.select(kb.getRoot(), new Equals(item));
  }

  public void add(Ast item) {
    // TODO make sure name does not exist
    kb.getRoot().children.add(item);
  }

}
