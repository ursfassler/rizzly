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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pass.FunPass;
import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Named;
import fun.other.Namespace;
import fun.other.Template;
import fun.type.Type;
import fun.type.template.ArrayTemplate;
import fun.type.template.RangeTemplate;
import fun.type.template.TypeTypeTemplate;

/**
 * Check if a reserved name is used.
 *
 * @author urs
 *
 */
public class CheckNames extends FunPass {
  // TODO find more elegant way to check Template names
  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    FunList<Type> blacklist = root.getItems(Type.class, false);

    FunList<Fun> tocheck = new FunList<Fun>(root.getChildren());
    tocheck.removeAll(blacklist);

    Set<String> names = new HashSet<String>(blacklist.names());
    names.addAll(getTemplateNames());

    CheckNamesWorker checkNames = new CheckNamesWorker();
    for (Fun itr : tocheck) {
      checkNames.traverse(itr, names);
    }
  }

  private Set<String> getTemplateNames() {
    Set<String> ret = new HashSet<String>();
    ret.add(RangeTemplate.NAME);
    ret.add(ArrayTemplate.NAME);
    ret.add(TypeTypeTemplate.NAME);
    return ret;
  }
}

class CheckNamesWorker extends DefTraverser<Void, Collection<String>> {

  @Override
  protected Void visit(Fun obj, Collection<String> param) {
    if (obj instanceof Named) {
      if (param.contains(((Named) obj).getName()) && !(obj instanceof Template)) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + ((Named) obj).getName());
      }
    }
    return super.visit(obj, param);
  }
}
