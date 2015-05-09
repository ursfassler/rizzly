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
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.ArithmeticOp;
import ast.data.expression.binop.BinaryExp;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BoolValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.data.reference.Reference;
import ast.traverser.NullTraverser;
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

class ConstTraverser extends NullTraverser<Boolean, Void> {

  @Override
  protected Boolean visitDefault(Ast obj, Void param) {
    RError.err(ErrorType.Fatal, obj.getInfo(), "KnowConst not implemented for " + obj.getClass().getCanonicalName());
    return null;
  }

  @Override
  protected Boolean visitRefExpr(RefExp obj, Void param) {
    return visit(obj.ref, param);
  }

  @Override
  protected Boolean visitBinaryExp(BinaryExp obj, Void param) {
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
  protected Boolean visitReference(Reference obj, Void param) {
    RError.err(ErrorType.Warning, obj.getInfo(), "fix me"); // TODO follow
    // reference
    return false;
  }

  @Override
  protected Boolean visitBoolValue(BoolValue obj, Void param) {
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
