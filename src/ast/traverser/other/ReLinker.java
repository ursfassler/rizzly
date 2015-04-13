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

package ast.traverser.other;

import java.util.Map;

import ast.data.Ast;
import ast.data.Named;
import ast.data.expression.reference.BaseRef;
import ast.traverser.DefTraverser;

public class ReLinker extends DefTraverser<Void, Map<Ast, Ast>> {

  public static void process(Ast classes, Map<Ast, Ast> map) {
    ReLinker reLinker = new ReLinker();
    reLinker.traverse(classes, map);
  }

  @Override
  protected Void visitBaseRef(BaseRef obj, Map<Ast, Ast> param) {
    Ast target = param.get(obj.link);
    if (target != null) {
      obj.link = (Named) target;
    }
    return super.visitBaseRef(obj, param);
  }

}
