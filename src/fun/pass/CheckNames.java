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

import pass.FunPass;
import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.knowledge.KnowledgeBase;
import fun.other.FunList;
import fun.other.Named;
import fun.other.Namespace;
import fun.type.Type;

public class CheckNames extends FunPass {

  @Override
  public void process(Namespace root, KnowledgeBase kb) {
    FunList<Type> blacklist = root.getItems(Type.class, false);

    FunList<Fun> tocheck = new FunList<Fun>(root.getChildren());
    tocheck.removeAll(blacklist);

    CheckNamesWorker checkNames = new CheckNamesWorker();
    for (Fun itr : tocheck) {
      checkNames.traverse(itr, blacklist.names());
    }
  }

}

class CheckNamesWorker extends DefTraverser<Void, Collection<String>> {

  @Override
  protected Void visit(Fun obj, Collection<String> param) {
    if (obj instanceof Named) {
      if (param.contains(((Named) obj).getName())) {
        RError.err(ErrorType.Error, obj.getInfo(), "Expected name, got keyword " + ((Named) obj).getName());
      }
    }
    return super.visit(obj, param);
  }

}
