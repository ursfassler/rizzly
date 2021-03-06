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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.data.AstList;
import ast.data.function.Function;
import ast.data.type.TypeRefFactory;
import ast.data.type.base.ArrayType;
import ast.data.type.base.ArrayTypeFactory;
import ast.data.type.base.EnumElement;
import ast.data.type.base.EnumType;
import ast.data.type.base.EnumTypeFactory;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.type.composed.UnionType;
import ast.knowledge.KnowPath;
import ast.knowledge.KnowledgeBase;
import ast.meta.MetaList;

class QueueTypes {
  private ArrayType queue;
  private UnionType message;
  private EnumType msgType;

  final private Map<Function, EnumElement> funcToType = new HashMap<Function, EnumElement>();
  final private Map<Function, NamedElement> funcToElem = new HashMap<Function, NamedElement>();
  final private Map<Function, RecordType> funcToRecord;

  final private String prefix;
  final private MetaList info;
  final private KnowPath kpath;

  public QueueTypes(String prefix, Map<Function, RecordType> funcToRecord, MetaList info, KnowledgeBase kb) {
    super();
    this.prefix = prefix;
    this.funcToRecord = funcToRecord;
    this.info = info;
    this.kpath = kb.getEntry(KnowPath.class);
  }

  public void create(int queueLength) {
    msgType = EnumTypeFactory.create(info, prefix + "msgid");

    AstList<NamedElement> unielem = new AstList<NamedElement>();
    for (Function func : getQueuedFunctions()) {
      // TODO better name
      EnumElement enumElem = createElemFromFunc(unielem, func);

      msgType.element.add(enumElem);
      funcToType.put(func, enumElem);
    }

    NamedElement tag = new NamedElement(info, "_tag", TypeRefFactory.create(info, msgType));
    message = new UnionType(prefix + "queue", unielem, tag);
    message.metadata().add(info);

    queue = createQueueType(queueLength, message);
  }

  private ArrayType createQueueType(int queueSize, UnionType uni) {
    return ArrayTypeFactory.create(queueSize, uni);
  }

  private EnumElement createElemFromFunc(AstList<NamedElement> unielem, Function func) {
    Designator path = kpath.get(func);
    assert (path.size() > 0);
    String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);

    NamedElement elem = new NamedElement(info, name, TypeRefFactory.create(info, funcToRecord.get(func)));
    funcToElem.put(func, elem);
    unielem.add(elem);

    EnumElement enumElem = new EnumElement(info, prefix + name);
    return enumElem;
  }

  public int queueLength() {
    return queue.size.intValue();
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
