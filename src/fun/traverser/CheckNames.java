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

package fun.traverser;

import java.util.Collection;

import error.ErrorType;
import error.RError;
import fun.DefTraverser;
import fun.Fun;
import fun.other.Named;

public class CheckNames extends DefTraverser<Void, Collection<String>> {

  static public void check(Fun fun, Collection<String> blacklist) {
    CheckNames checkNames = new CheckNames();
    checkNames.traverse(fun, blacklist);
  }

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
