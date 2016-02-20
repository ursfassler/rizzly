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
import ast.data.component.composition.EndpointRaw;
import ast.data.expression.ReferenceExpression;
import ast.data.function.ret.FunctionReturnType;
import ast.data.reference.OffsetReference;
import ast.data.reference.RefItem;
import ast.data.reference.Reference;
import ast.data.statement.AssignmentSingle;
import ast.data.statement.MsgPush;
import ast.data.statement.MultiAssignment;
import ast.data.type.out.AliasType;
import ast.data.variable.FunctionVariable;
import ast.dispatcher.DfsTraverser;

@Deprecated
abstract public class RefReplacer<T> extends DfsTraverser<Reference, T> {

  // TODO delete RefReplacer or implement following methods
  // @Override
  // protected Reference visitSubCallbacks(SubCallbacks obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitEndpointSelf(EndpointSelf obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitEndpointSub(EndpointSub obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitImplElementary(ImplElementary obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitStateSimple(StateSimple obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitStateComposite(StateComposite obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitTransition(Transition obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitTypeCast(TypeCast obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitUnionValue(UnionValue obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitUnsafeUnionValue(UnsafeUnionValue obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitRecordValue(RecordValue obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitCallStmt(CallStmt obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitArrayType(ArrayType obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitFunctionType(FunctionType obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitNamedElement(NamedElement obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitTypeType(TypeType obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitStateVariable(StateVariable obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitConstPrivate(ConstPrivate obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitConstGlobal(GlobalConstant obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }
  //
  // @Override
  // protected Reference visitTemplateParameter(TemplateParameter obj, T param) {
  // throw new RuntimeException("not yet implemented");
  // }

  @Override
  protected Reference visitFuncVariable(FunctionVariable obj, T param) {
    obj.type = visit(obj.type, param);
    return null;
  }

  @Override
  protected Reference visitEndpointRaw(EndpointRaw obj, T param) {
    obj.ref = visit(obj.ref, param);
    return null;
  }

  @Override
  protected Reference visitFuncReturnType(FunctionReturnType obj, T param) {
    obj.type = visit(obj.type, param);
    return null;
  }

  @Override
  protected Reference visitAliasType(AliasType obj, T param) {
    obj.ref = visit(obj.ref, param);
    return null;
  }

  @Override
  protected Reference visitOffsetReference(OffsetReference obj, T param) {
    for (RefItem item : obj.getOffset()) {
      visit(item, param);
    }
    return obj;
  }

  @Override
  protected Reference visitRefExpr(ReferenceExpression obj, T param) {
    obj.reference = visit(obj.reference, param);
    return null;
  }

  @Override
  protected Reference visitAssignmentMulti(MultiAssignment obj, T param) {
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
