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

package evl.queue;

import java.math.BigInteger;

import common.ElementInfo;

import evl.expression.ArrayValue;
import evl.expression.Expression;
import evl.expression.Number;
import evl.expression.reference.SimpleRef;
import evl.knowledge.KnowBaseItem;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.variable.StateVariable;

class QueueVariables {
  private StateVariable queue;
  private StateVariable head;
  private StateVariable count;

  final private String prefix;
  final private ElementInfo info;
  final private KnowBaseItem kbi;

  public QueueVariables(String prefix, ElementInfo info, KnowledgeBase kb) {
    super();
    this.prefix = prefix;
    this.info = info;
    this.kbi = kb.getEntry(KnowBaseItem.class);
  }

  void create(ArrayType queueType) {
    queue = new StateVariable(info, makeName("vdata"), makeRef(queueType), makeQueueDefaultValue());
    head = new StateVariable(info, makeName("vhead"), makeRef(kbi.getRangeType(queueLength(queueType))), makeNumberZero());
    count = new StateVariable(info, makeName("vcount"), makeRef(kbi.getRangeType(queueLength(queueType) + 1)), makeNumberZero());
  }

  private ArrayValue makeQueueDefaultValue() {
    return new ArrayValue(info, new EvlList<Expression>());
  }

  private SimpleRef<Type> makeRef(Type type) {
    return new SimpleRef<Type>(info, type);
  }

  private String makeName(String name) {
    return prefix + name;
  }

  private Number makeNumberZero() {
    return new Number(info, BigInteger.ZERO);
  }

  private int queueLength(ArrayType queueType) {
    return queueType.getSize().intValue();
  }

  public StateVariable getQueue() {
    return queue;
  }

  public StateVariable getHead() {
    return head;
  }

  public StateVariable getCount() {
    return count;
  }

}
