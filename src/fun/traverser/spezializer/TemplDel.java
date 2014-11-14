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

package fun.traverser.spezializer;

import java.util.HashSet;
import java.util.Set;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.FunList;
import fun.other.Template;

/**
 * Tries to remove all templates. Detects missed ones.
 */
public class TemplDel extends DefTraverser<Void, Void> {
  static private final TemplDel INSTANCE = new TemplDel();

  public static void process(Fun fun) {
    INSTANCE.traverse(fun, null);
  }

  @Override
  protected Void visitList(FunList<? extends Fun> list, Void param) {
    Set<Template> remove = new HashSet<Template>();
    for (Fun ast : list) {
      if (ast instanceof Template) {
        remove.add((Template) ast);
      } else {
        visit(ast, param);
      }
    }
    list.removeAll(remove);
    return null;
  }

  @Override
  protected Void visitDeclaration(Template obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "missed template");
    return null;
  }

}
