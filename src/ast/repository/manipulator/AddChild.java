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

package ast.repository.manipulator;

import ast.data.Ast;
import ast.data.Namespace;
import ast.data.component.hfsm.State;
import ast.data.component.hfsm.StateContent;
import ast.data.raw.RawElementary;
import ast.data.template.Template;
import ast.dispatcher.NullDispatcher;
import ast.knowledge.KnowParent;
import ast.knowledge.KnowledgeBase;

public class AddChild {

  static public void add(Ast child, KnowledgeBase kb) {
    KnowParent kp = kb.getEntry(KnowParent.class);
    Ast parent = kp.get(child);
    add(parent, child);
  }

  static public void add(Ast parent, Ast child) {
    AddChildDispatcher.add(parent, child);
  }
}

class AddChildDispatcher extends NullDispatcher<Void, Ast> {
  final static private AddChildDispatcher INSTANCE = new AddChildDispatcher();

  static void add(Ast parent, Ast child) {
    INSTANCE.traverse(parent, child);
  }

  @Override
  protected Void visitDefault(Ast obj, Ast param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getName());
  }

  @Override
  protected Void visitNamespace(Namespace obj, Ast param) {
    obj.children.add(param);
    return null;
  }

  @Override
  protected Void visitRawElementary(RawElementary obj, Ast param) {
    assert (!(param instanceof Template));
    obj.getInstantiation().add(param);
    return null;
  }

  @Override
  protected Void visitState(State obj, Ast param) {
    obj.item.add((StateContent) param);
    return null;
  }

}
