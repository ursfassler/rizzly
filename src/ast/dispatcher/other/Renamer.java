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

package ast.dispatcher.other;

import java.util.Set;

import ast.Designator;
import ast.data.Ast;
import ast.data.Named;
import ast.dispatcher.DfsTraverser;

//TODO how to ensure that names are unique?
//TODO use blacklist with keywords
//TODO use better names for public stuff
/**
 * @see pir.traverser.Renamer
 *
 * @author urs
 *
 */
public class Renamer extends DfsTraverser<Void, Void> {

  final private Set<String> blacklist;

  public Renamer(Set<String> blacklist) {
    super();
    this.blacklist = blacklist;
  }

  public static void process(Ast cprog, Set<String> blacklist) {
    Renamer cVarDeclToTop = new Renamer(blacklist);
    cVarDeclToTop.traverse(cprog, null);
  }

  private String cleanName(String name) {
    String ret = ast.pass.others.CRenamer.cleanName(name);

    while (blacklist.contains(ret.toLowerCase())) {
      ret += Designator.NAME_SEP;
    }

    return ret;
  }

  @Override
  protected Void visit(Ast obj, Void param) {
    if (obj instanceof Named) {
      Named item = (Named) obj;
      String name = cleanName(item.getName());
      item.setName(name);
    }
    return super.visit(obj, param);
  }

}
