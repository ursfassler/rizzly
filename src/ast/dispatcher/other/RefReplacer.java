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

package ast.dispatcher.other;

import ast.data.AstList;
import ast.data.expression.RefExp;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.reference.TypedRef;
import ast.data.statement.AssignmentMulti;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.MsgPush;
import ast.dispatcher.DfsTraverser;

abstract public class RefReplacer<T> extends DfsTraverser<Reference, T> {

  @Override
  protected Reference visitReference(Reference obj, T param) {
    for (RefItem item : obj.offset) {
      visit(item, param);
    }
    return obj;
  }

  @Override
  protected Reference visitTypedRef(TypedRef obj, T param) {
    obj.ref = visit(obj.ref, param);
    return null;
  }

  @Override
  protected Reference visitRefExpr(RefExp obj, T param) {
    obj.ref = visit(obj.ref, param);
    return null;
  }

  @Override
  protected Reference visitAssignmentMulti(AssignmentMulti obj, T param) {
    visitRefList(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected Reference visitAssignmentSingle(AssignmentSingle obj, T param) {
    obj.left = visit(obj.left, param);
    visit(obj.right, param);
    return null;
  }

  @Override
  protected Reference visitMsgPush(MsgPush obj, T param) {
    obj.queue = visit(obj.queue, param);
    visit(obj.func, param);
    visitList(obj.data, param);
    return null;
  }

  private <R extends Reference> void visitRefList(AstList<R> list, T param) {
    for (int i = 0; i < list.size(); i++) {
      Reference old = list.get(i);
      Reference expr = visit(old, param);
      list.set(i, (R) expr);
    }
  }
}
