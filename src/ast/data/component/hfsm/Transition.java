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

package ast.data.component.hfsm;

import ast.data.AstBase;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.reference.Reference;
import ast.data.statement.Block;
import ast.data.variable.FunctionVariable;

final public class Transition extends AstBase implements StateContent {
  public Reference src;
  public Reference dst;
  public Reference eventFunc;
  final public AstList<FunctionVariable> param = new AstList<FunctionVariable>();
  public Expression guard;
  public Block body;

  public Transition(Reference src, Reference dst, Reference eventFunc, Expression guard, AstList<FunctionVariable> param, Block body) {
    this.src = src;
    this.dst = dst;
    this.eventFunc = eventFunc;
    this.param.addAll(param);
    this.guard = guard;
    this.body = body;
  }

  @Override
  public String toString() {
    return src + " -> " + dst + " by " + eventFunc + param + " if " + guard;
  }

}
