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

import ast.data.AstBase;
import ast.data.Namespace;
import ast.knowledge.KnowledgeBase;
import ast.traverser.other.ClassGetter;

public class NoItem extends Condition {
  final private Class<? extends AstBase> type;

  public NoItem(Class<? extends AstBase> type) {
    super();
    this.type = type;
  }

  public Class<? extends AstBase> getType() {
    return type;
  }

  @Override
  public boolean check(Namespace root, KnowledgeBase kb) {
    return ClassGetter.getRecursive(type, root).isEmpty();
  }

  @Override
  public String getName() {
    return "NoItem<" + type.getName() + ">";
  }
}
