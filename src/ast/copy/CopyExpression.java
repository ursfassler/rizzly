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

package ast.copy;

import java.util.ArrayList;

import ast.data.Ast;
import ast.data.expression.Expression;
import ast.data.expression.RefExp;
import ast.data.expression.TypeCast;
import ast.data.expression.binop.And;
import ast.data.expression.binop.BitAnd;
import ast.data.expression.binop.BitOr;
import ast.data.expression.binop.BitXor;
import ast.data.expression.binop.Division;
import ast.data.expression.binop.Equal;
import ast.data.expression.binop.Greater;
import ast.data.expression.binop.GreaterEqual;
import ast.data.expression.binop.Is;
import ast.data.expression.binop.Less;
import ast.data.expression.binop.LessEqual;
import ast.data.expression.binop.LogicAnd;
import ast.data.expression.binop.LogicOr;
import ast.data.expression.binop.Minus;
import ast.data.expression.binop.Modulo;
import ast.data.expression.binop.Multiplication;
import ast.data.expression.binop.NotEqual;
import ast.data.expression.binop.Or;
import ast.data.expression.binop.Plus;
import ast.data.expression.binop.Shl;
import ast.data.expression.binop.Shr;
import ast.data.expression.unop.BitNot;
import ast.data.expression.unop.LogicNot;
import ast.data.expression.unop.Not;
import ast.data.expression.unop.Uminus;
import ast.data.expression.value.AnyValue;
import ast.data.expression.value.ArrayValue;
import ast.data.expression.value.BooleanValue;
import ast.data.expression.value.NamedElementsValue;
import ast.data.expression.value.NamedValue;
import ast.data.expression.value.NumberValue;
import ast.data.expression.value.RecordValue;
import ast.data.expression.value.StringValue;
import ast.data.expression.value.TupleValue;
import ast.data.expression.value.UnionValue;
import ast.data.expression.value.UnsafeUnionValue;
import ast.dispatcher.NullDispatcher;

public class CopyExpression extends NullDispatcher<Expression, Void> {

  private CopyAst cast;

  public CopyExpression(CopyAst cast) {
    super();
    this.cast = cast;
  }

  @Override
  protected Expression visitDefault(Ast obj, Void param) {
    throw new RuntimeException("not yet implemented: " + obj.getClass().getCanonicalName());
  }

  @Override
  protected Expression visitRefExpr(RefExp obj, Void param) {
    return new RefExp(obj.getInfo(), cast.copy(obj.ref));
  }

  @Override
  protected Expression visitTypeCast(TypeCast obj, Void param) {
    return new TypeCast(obj.getInfo(), cast.copy(obj.cast), cast.copy(obj.value));
  }

  @Override
  protected Expression visitBoolValue(BooleanValue obj, Void param) {
    return new BooleanValue(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitAnyValue(AnyValue obj, Void param) {
    return new AnyValue(obj.getInfo());
  }

  @Override
  protected Expression visitNumber(NumberValue obj, Void param) {
    return new NumberValue(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitStringValue(StringValue obj, Void param) {
    return new StringValue(obj.getInfo(), obj.value);
  }

  @Override
  protected Expression visitArrayValue(ArrayValue obj, Void param) {
    return new ArrayValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitTupleValue(TupleValue obj, Void param) {
    return new TupleValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitRecordValue(RecordValue obj, Void param) {
    return new RecordValue(obj.getInfo(), new ArrayList<NamedValue>(cast.copy(obj.value)), cast.copy(obj.type));
  }

  @Override
  protected Expression visitNamedElementsValue(NamedElementsValue obj, Void param) {
    return new NamedElementsValue(obj.getInfo(), cast.copy(obj.value));
  }

  @Override
  protected Expression visitUnsafeUnionValue(UnsafeUnionValue obj, Void param) {
    return new UnsafeUnionValue(obj.getInfo(), cast.copy(obj.contentValue), cast.copy(obj.type));
  }

  @Override
  protected Expression visitUnionValue(UnionValue obj, Void param) {
    return new UnionValue(obj.getInfo(), cast.copy(obj.tagValue), cast.copy(obj.contentValue), cast.copy(obj.type));
  }

  @Override
  protected Expression visitMinus(Minus obj, Void param) {
    return new Minus(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitAnd(And obj, Void param) {
    return new And(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitDiv(Division obj, Void param) {
    return new Division(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitEqual(Equal obj, Void param) {
    return new Equal(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitGreater(Greater obj, Void param) {
    return new Greater(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLess(Less obj, Void param) {
    return new Less(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitGreaterequal(GreaterEqual obj, Void param) {
    return new GreaterEqual(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLessequal(LessEqual obj, Void param) {
    return new LessEqual(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitIs(Is obj, Void param) {
    return new Is(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitMod(Modulo obj, Void param) {
    return new Modulo(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitMul(Multiplication obj, Void param) {
    return new Multiplication(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitNotequal(NotEqual obj, Void param) {
    return new NotEqual(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitOr(Or obj, Void param) {
    return new Or(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitPlus(Plus obj, Void param) {
    return new Plus(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitShl(Shl obj, Void param) {
    return new Shl(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitShr(Shr obj, Void param) {
    return new Shr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitNot(Not obj, Void param) {
    return new Not(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitLogicNot(LogicNot obj, Void param) {
    return new LogicNot(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitBitNot(BitNot obj, Void param) {
    return new BitNot(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitUminus(Uminus obj, Void param) {
    return new Uminus(obj.getInfo(), cast.copy(obj.expr));
  }

  @Override
  protected Expression visitBitAnd(BitAnd obj, Void param) {
    return new BitAnd(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitBitOr(BitOr obj, Void param) {
    return new BitOr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitBitXor(BitXor obj, Void param) {
    return new BitXor(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLogicOr(LogicOr obj, Void param) {
    return new LogicOr(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

  @Override
  protected Expression visitLogicAnd(LogicAnd obj, Void param) {
    return new LogicAnd(obj.getInfo(), cast.copy(obj.left), cast.copy(obj.right));
  }

}
