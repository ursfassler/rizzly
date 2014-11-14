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

package evl.statement.intern;

import java.util.Collection;

import common.ElementInfo;

import evl.expression.Expression;
import evl.expression.reference.Reference;
import evl.other.EvlList;
import evl.statement.Statement;

public class MsgPush extends Statement {
  private Reference queue;
  private Reference func;
  private final EvlList<Expression> data = new EvlList<Expression>();

  public MsgPush(ElementInfo info, Reference queue, Reference func, Collection<Expression> data) {
    super(info);
    this.queue = queue;
    this.func = func;
    this.data.addAll(data);
  }

  public Reference getQueue() {
    return queue;
  }

  public void setQueue(Reference queue) {
    this.queue = queue;
  }

  public Reference getFunc() {
    return func;
  }

  public void setFunc(Reference func) {
    this.func = func;
  }

  public EvlList<Expression> getData() {
    return data;
  }

  @Override
  public String toString() {
    return queue + ".push(" + func + "," + data + ")";
  }

}
