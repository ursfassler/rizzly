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

import java.util.HashMap;
import java.util.Map;

import ast.Designator;
import ast.copy.Copy;
import ast.data.AstList;
import ast.data.Namespace;
import ast.data.function.Function;
import ast.data.statement.MsgPush;
import ast.data.type.composed.NamedElement;
import ast.data.type.composed.RecordType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.DfsTraverser;
import ast.repository.query.Referencees.TargetResolver;

class CreateRecordFromFunc extends DfsTraverser<Void, Void> {
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
    Function func = TargetResolver.staticTargetOf(obj.func, Function.class);
    // Designator path = kp.get(func);
    // assert (path.size() > 0);
    // String name = new Designator(path,
    // func.getName()).toString(Designator.NAME_SEP);
    String name = Integer.toString(func.hashCode());

    AstList<NamedElement> elements = new AstList<NamedElement>();
    for (FunctionVariable arg : func.param) {
      NamedElement elem = new NamedElement(arg.metadata(), arg.getName(), Copy.copy(arg.type));
      elements.add(elem);
    }
    RecordType rec = new RecordType(func.metadata(), Designator.NAME_SEP + "msg" + Designator.NAME_SEP + name, elements);

    getMapping().put(func, rec);
    root.children.add(rec);

    return null;
  }

}
