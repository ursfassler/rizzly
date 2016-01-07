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

package ast.data.statement;

import java.util.Collection;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.expression.Expression;
import ast.data.function.FuncRef;
import ast.data.reference.Reference;
import ast.visitor.Visitor;

final public class MsgPush extends Statement {
  public Reference queue;
  public FuncRef func;
  final public AstList<Expression> data = new AstList<Expression>();

  public MsgPush(ElementInfo info, Reference queue, FuncRef func, Collection<Expression> data) {
    super(info);
    this.queue = queue;
    this.func = func;
    this.data.addAll(data);
  }

  @Override
  public String toString() {
    return queue + ".push(" + func + "," + data + ")";
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
