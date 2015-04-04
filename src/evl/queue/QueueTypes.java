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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.expression.reference.SimpleRef;
import evl.function.Function;
import evl.knowledge.KnowParent;
import evl.knowledge.KnowledgeBase;
import evl.other.EvlList;
import evl.type.Type;
import evl.type.base.ArrayType;
import evl.type.base.EnumElement;
import evl.type.base.EnumType;
import evl.type.composed.NamedElement;
import evl.type.composed.RecordType;
import evl.type.composed.UnionType;

class QueueTypes {
  private ArrayType queue;
  private UnionType message;
  private EnumType msgType;

  final private Map<Function, EnumElement> funcToType = new HashMap<Function, EnumElement>();
  final private Map<Function, NamedElement> funcToElem = new HashMap<Function, NamedElement>();
  final private Map<Function, RecordType> funcToRecord;

  final private String prefix;
  final private ElementInfo info;
  final private KnowParent kp;

  public QueueTypes(String prefix, Map<Function, RecordType> funcToRecord, ElementInfo info, KnowledgeBase kb) {
    super();
    this.prefix = prefix;
    this.funcToRecord = funcToRecord;
    this.info = info;
    this.kp = kb.getEntry(KnowParent.class);
  }

  public void create(int queueLength) {
    msgType = new EnumType(info, prefix + "msgid");

    EvlList<NamedElement> unielem = new EvlList<NamedElement>();
    for (Function func : getQueuedFunctions()) {
      // TODO better name
      EnumElement enumElem = createElemFromFunc(unielem, func);

      msgType.getElement().add(enumElem);
      funcToType.put(func, enumElem);
    }

    NamedElement tag = new NamedElement(info, "_tag", new SimpleRef<Type>(info, msgType));
    message = new UnionType(info, prefix + "queue", unielem, tag);

    queue = createQueueType(queueLength, message);
  }

  private ArrayType createQueueType(int queueSize, UnionType uni) {
    return new ArrayType(BigInteger.valueOf(queueSize), new SimpleRef<Type>(info, uni));
  }

  private EnumElement createElemFromFunc(EvlList<NamedElement> unielem, Function func) {
    Designator path = kp.getPath(func);
    assert (path.size() > 0);
    String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);

    NamedElement elem = new NamedElement(info, name, new SimpleRef<Type>(info, funcToRecord.get(func)));
    funcToElem.put(func, elem);
    unielem.add(elem);

    EnumElement enumElem = new EnumElement(info, prefix + name);
    return enumElem;
  }

  public int queueLength() {
    return queue.getSize().intValue();
  }

  public ArrayType getQueue() {
    return queue;
  }

  public UnionType getMessage() {
    return message;
  }

  public EnumType getMsgType() {
    return msgType;
  }

  public Map<Function, EnumElement> getFuncToMsgType() {
    return funcToType;
  }

  public Map<Function, NamedElement> getFuncToElem() {
    return funcToElem;
  }

  public Map<Function, RecordType> getFuncToRecord() {
    return funcToRecord;
  }

  public Collection<Function> getQueuedFunctions() {
    return funcToRecord.keySet();
  }
}
