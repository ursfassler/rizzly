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

package ast.specification;

import ast.data.Ast;
import ast.data.expression.ReferenceExpression;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.ArithmeticOp;
import ast.data.expression.binop.BinaryExpression;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.reference.OffsetReference;
import ast.dispatcher.NullDispatcher;
import error.ErrorType;
import error.RError;

public class ConstantExpression extends Specification {
  static public final ConstantExpression INSTANCE = new ConstantExpression();
  static private final ConstTraverser ct = new ConstTraverser();

  @Override
  public boolean isSatisfiedBy(Ast candidate) {
    return ct.traverse(candidate, null);
  }

}

class ConstTraverser extends NullDispatcher<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    RError.err(ErrorType.Fatal, "KnowConst not implemented for " + obj.getClass().getCanonicalName(), obj.metadata());
    return null;
  }

  @Override
  protected Boolean visitRefExpr(ReferenceExpression obj, Void param) {
    return visit(obj.reference, param);
  }

  @Override
  protected Boolean visitBinaryExp(BinaryExpression obj, Void param) {
    return visit(obj.left, param) && visit(obj.right, param);
  }

  @Override
  protected Boolean visitArithmeticOp(ArithmeticOp obj, Void param) {
    return visit(obj.left, param) && visit(obj.right, param);
  }

  @Override
  protected Boolean visitTypeCast(TypeCast obj, Void param) {
    return visit(obj.value, param);
  }

  @Override
  protected Boolean visitOffsetReference(OffsetReference obj, Void param) {
    RError.err(ErrorType.Warning, "fix me", obj.metadata()); // TODO follow reference
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BooleanValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitRecordValue(RecordValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitTupleValue(TupleValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitUnionValue(UnionValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitArrayValue(ArrayValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitNumber(NumberValue obj, Void param) {
    return true;
  }

  @Override
  protected Boolean visitStringValue(StringValue obj, Void param) {
    return true;
  }

}
