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

import evl.data.EvlList;
import evl.data.Namespace;
import evl.data.expression.reference.SimpleRef;
import evl.data.function.Function;
import evl.data.statement.intern.MsgPush;
import evl.data.type.Type;
import evl.data.type.composed.NamedElement;
import evl.data.type.composed.RecordType;
import evl.data.variable.FuncVariable;
import evl.traverser.DefTraverser;

class CreateRecordFromFunc extends DefTraverser<Void, Void> {
  final private Map<Function, RecordType> funcToRecord = new HashMap<Function, RecordType>();

  final private Namespace root;

  public CreateRecordFromFunc(Namespace root) {
    super();
    this.root = root;
  }

  public Map<Function, RecordType> getMapping() {
    return funcToRecord;
  }

  @Override
  protected Void visitMsgPush(MsgPush obj, Void param) {
    Function func = (Function) obj.func.link;
    // Designator path = kp.get(func);
    // assert (path.size() > 0);
    // String name = new Designator(path, func.getName()).toString(Designator.NAME_SEP);
    String name = Integer.toString(func.hashCode());

    EvlList<NamedElement> elements = new EvlList<NamedElement>();
    for (FuncVariable arg : func.param) {
      NamedElement elem = new NamedElement(arg.getInfo(), arg.name, new SimpleRef<Type>(ElementInfo.NO, arg.type.link));
      elements.add(elem);
    }
    RecordType rec = new RecordType(func.getInfo(), Designator.NAME_SEP + "msg" + Designator.NAME_SEP + name, elements);

    getMapping().put(func, rec);
    root.children.add(rec);

    return null;
  }

}
