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

package evl.traverser.typecheck.specific;

import evl.function.Function;
import evl.knowledge.KnowledgeBase;
import evl.traverser.typecheck.TypeChecker;
import evl.type.Type;
import evl.variable.Variable;

public class FunctionTypeChecker {

  public static void process(Function obj, KnowledgeBase kb) {
    for (Variable param : obj.getParam()) {
      TypeChecker.process(param, kb);
    }
    Type ret;
    ret = obj.getRet().getLink();
    StatementTypeChecker.process(obj.getBody(), ret, kb);
  }

}
