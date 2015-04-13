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

import ast.ElementInfo;
import ast.data.AstBase;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.expression.reference.FuncRef;
import ast.data.expression.reference.StateRef;
import ast.data.statement.Block;
import ast.data.variable.FuncVariable;

public class Transition extends AstBase implements StateContent {
  public StateRef src;
  public StateRef dst;
  public FuncRef eventFunc;
  final public AstList<FuncVariable> param = new AstList<FuncVariable>();
  public Expression guard;
  public Block body;

  static public Transition create(ElementInfo info) {
    return new Transition(info, null, null, null, null, new AstList<FuncVariable>(), new Block(info));
  }

  public Transition(ElementInfo info, StateRef src, StateRef dst, FuncRef eventFunc, Expression guard, AstList<FuncVariable> param, Block body) {
    super(info);
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
