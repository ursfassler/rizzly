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

package evl.data.statement.intern;

import java.util.Collection;

import common.ElementInfo;

import evl.data.EvlList;
import evl.data.expression.Expression;
import evl.data.expression.reference.Reference;
import evl.data.statement.Statement;

public class MsgPush extends Statement {
  public Reference queue;
  public Reference func;
  final public EvlList<Expression> data = new EvlList<Expression>();

  public MsgPush(ElementInfo info, Reference queue, Reference func, Collection<Expression> data) {
    super(info);
    this.queue = queue;
    this.func = func;
    this.data.addAll(data);
  }

  @Override
  public String toString() {
    return queue + ".push(" + func + "," + data + ")";
  }

}
