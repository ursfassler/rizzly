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

package evl.pass.queue;

import java.util.HashMap;
import java.util.Map;

import common.Designator;
import common.ElementInfo;

import evl.copy.Relinker;
import evl.data.Namespace;
import evl.data.component.composition.Queue;
import evl.data.function.Function;
import evl.data.type.composed.RecordType;
import evl.knowledge.KnowledgeBase;
import evl.traverser.other.ClassGetter;

class QueueReductionWorker {
  private static final int queueLength = 20;  // FIXME remove magic

  final private Namespace root;

  final private KnowledgeBase kb;

  public QueueReductionWorker(Namespace root, KnowledgeBase kb) {
    super();
    this.root = root;
    this.kb = kb;
  }

  public void process() {
    Queue queue = reduceQueuesToOne();
    root.add(queue);

    CreateRecordFromFunc createMsgContentTypes = new CreateRecordFromFunc(root);
    createMsgContentTypes.traverse(root, null);
    Map<Function, RecordType> funcToRecord = createMsgContentTypes.getMapping();

    if (funcToRecord.isEmpty()) {
      return;
    }

    String prefix = queue.getName() + Designator.NAME_SEP;

    Map<Function, Function> pushfunc = createQueue(prefix, funcToRecord, queue.getInfo());

    PushReplacer pr = new PushReplacer();
    pr.traverse(root, pushfunc);
  }

  private Queue reduceQueuesToOne() {
    // TODO keep queues of active components
    Queue queue = new Queue();
    Map<Queue, Queue> map = mapQueuesToOne(queue);
    Relinker.relink(root, map);
    return queue;
  }

  private Map<Queue, Queue> mapQueuesToOne(Queue queue) {
    Map<Queue, Queue> map = new HashMap<Queue, Queue>();
    for (Queue old : ClassGetter.get(Queue.class, root)) {
      map.put(old, queue);
    }
    return map;
  }

  private Map<Function, Function> createQueue(String prefix, Map<Function, RecordType> funcToRecord, ElementInfo info) {
    QueueTypes queueTypes = new QueueTypes(prefix, funcToRecord, info, kb);
    queueTypes.create(queueLength);
    root.add(queueTypes.getMsgType());
    root.add(queueTypes.getMessage());
    root.add(queueTypes.getQueue());

    QueueVariables queueVariables = createQueue(prefix, info, queueTypes);
    return createPushFunctions(info, queueVariables, queueTypes);
  }

  private QueueVariables createQueue(String prefix, ElementInfo info, QueueTypes queueTypes) {
    QueueVariables queueVariables = new QueueVariables(prefix, info, kb);
    queueVariables.create(queueTypes.getQueue());
    root.add(queueVariables.getQueue());
    root.add(queueVariables.getHead());
    root.add(queueVariables.getCount());

    Function sizefunc = CountFunctionFactory.create(prefix, info, queueVariables);
    root.add(sizefunc);

    Function dispatcher = DispatchFunctionFactory.create(prefix, info, queueVariables, queueTypes);
    root.add(dispatcher);
    return queueVariables;
  }

  private Map<Function, Function> createPushFunctions(ElementInfo info, QueueVariables queueVariables, QueueTypes queueTypes) {
    Map<Function, Function> pushfunc = new HashMap<Function, Function>();
    for (Function func : queueTypes.getQueuedFunctions()) {
      Function impl = PushFunctionFactory.create(info, queueVariables, queueTypes, func);

      root.add(impl);
      pushfunc.put(func, impl);
    }
    return pushfunc;
  }

}
