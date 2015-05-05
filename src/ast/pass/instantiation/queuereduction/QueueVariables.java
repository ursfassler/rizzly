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

package ast.pass.instantiation.queuereduction;

import java.math.BigInteger;

import ast.ElementInfo;
import ast.data.AstList;
import ast.data.expression.ArrayValue;
import ast.data.expression.Expression;
import ast.data.expression.Number;
import ast.data.expression.reference.SimpleRef;
import ast.data.expression.reference.TypeRef;
import ast.data.type.Type;
import ast.data.type.base.ArrayType;
import ast.data.variable.StateVariable;
import ast.knowledge.KnowledgeBase;
import ast.manipulator.TypeRepo;

class QueueVariables {
  private StateVariable queue;
  private StateVariable head;
  private StateVariable count;

  final private String prefix;
  final private ElementInfo info;
  final private TypeRepo kbi;

  public QueueVariables(String prefix, ElementInfo info, KnowledgeBase kb) {
    super();
    this.prefix = prefix;
    this.info = info;
    this.kbi = new TypeRepo(kb);
  }

  void create(ArrayType queueType) {
    queue = new StateVariable(info, makeName("vdata"), makeRef(queueType), makeQueueDefaultValue());
    head = new StateVariable(info, makeName("vhead"), makeRef(kbi.getRangeType(queueLength(queueType))), makeNumberZero());
    count = new StateVariable(info, makeName("vcount"), makeRef(kbi.getRangeType(queueLength(queueType) + 1)), makeNumberZero());
  }

  private ArrayValue makeQueueDefaultValue() {
    return new ArrayValue(info, new AstList<Expression>());
  }

  private TypeRef makeRef(Type type) {
    return new SimpleRef<Type>(info, type);
  }

  private String makeName(String name) {
    return prefix + name;
  }

  private Number makeNumberZero() {
    return new Number(info, BigInteger.ZERO);
  }

  private int queueLength(ArrayType queueType) {
    return queueType.size.intValue();
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
